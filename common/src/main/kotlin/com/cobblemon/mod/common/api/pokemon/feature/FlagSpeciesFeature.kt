/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.aspect.FeatureAspectProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.client.gui.summary.featurerenderers.SummarySpeciesFeatureRenderer
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject
import kotlin.random.Random
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

/**
 * A simple [SpeciesFeature] that is a true/false flag value. It implements [CustomPokemonProperty] for use in
 * [PokemonProperties]. [FlagSpeciesFeatureProvider]s must be registered within [SpeciesFeatures].
 *
 * Implementations of this class don't need to implement anything.
 *
 * @author Hiroku
 * @since May 13th, 2022
 */
open class FlagSpeciesFeature(override val name: String) : SynchronizedSpeciesFeature<Boolean>, CustomPokemonProperty {
    constructor(name: String, enabled: Boolean): this(name) {
        this.value = enabled
    }

    override var value: Boolean = false
    override fun saveToNBT(pokemonNBT: NbtCompound): NbtCompound {
        pokemonNBT.putBoolean(name, value)
        return pokemonNBT
    }

    override fun loadFromNBT(pokemonNBT: NbtCompound): SpeciesFeature<Boolean> {
        value = if (pokemonNBT.contains(name)) pokemonNBT.getBoolean(name) else value
        return this
    }

    override fun saveToJSON(pokemonJSON: JsonObject): JsonObject {
        pokemonJSON.addProperty(name, value)
        return pokemonJSON
    }

    override fun loadFromJSON(pokemonJSON: JsonObject): SpeciesFeature<Boolean> {
        val isEnabled = pokemonJSON.get(name)?.asBoolean
        value = isEnabled ?: this.value
        return this
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(value)
    }

    override fun decode(buffer: PacketByteBuf) {
        value = buffer.readBoolean()
    }

    override fun asString() = "$name=$value"

    override fun apply(pokemon: Pokemon) {
        val featureProvider = SpeciesFeatures.getFeature(name) ?: return
        if (featureProvider in SpeciesFeatures.getFeaturesFor(pokemon.species)) {
            val existingFeature = pokemon.getFeature<FlagSpeciesFeature>(name)
            if (existingFeature != null) {
                existingFeature.value = value
            } else {
                pokemon.features.add(FlagSpeciesFeature(name, value))
            }
            pokemon.updateAspects()
        }
    }

    override fun matches(pokemon: Pokemon) = pokemon.getFeature<FlagSpeciesFeature>(name)?.value == value
}

class FlagSpeciesFeatureProvider : SynchronizedSpeciesFeatureProvider<FlagSpeciesFeature>, CustomPokemonPropertyType<FlagSpeciesFeature>, FeatureAspectProvider {
    override var keys: List<String> = emptyList()
    // Uses get() = true because that way there's no backing field. It MUST be true, this way no JSON trickery will overwrite it
    override val needsKey get() = true
    override var default: String? = null
    override var isAspect = true

    override fun matches(aspect: String): Boolean = this.isAspect && this.keys.first() == aspect

    override fun from(aspect: String): SpeciesFeature<*>? = if (matches(aspect)) fromString(null) else null

    override fun set(pokemon: Pokemon, aspect: String) {
        this.get(pokemon)?.value = true
    }

    override var visible: Boolean = false

    override fun invoke(buffer: PacketByteBuf, name: String): FlagSpeciesFeature? {
        return if (name in keys) {
            FlagSpeciesFeature(name).also { it.decode(buffer) }
        } else {
            null
        }
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeCollection(keys) { _, value -> buffer.writeString(value) }
        buffer.writeNullable(default) { _, value -> buffer.writeString(value) }
        buffer.writeBoolean(isAspect)
    }

    override fun decode(buffer: PacketByteBuf) {
        keys = buffer.readList { it.readString() }
        default = buffer.readNullable { it.readString() }
        isAspect = buffer.readBoolean()
    }

    override fun getRenderer(pokemon: Pokemon): SummarySpeciesFeatureRenderer<FlagSpeciesFeature>? {
        return null
    }

    override fun examples() = setOf("true", "false")

    internal constructor() {
        this.keys = emptyList()
    }

    constructor(keys: List<String>) {
        this.keys = keys
    }

    constructor(keys: List<String>, default: Boolean) {
        this.keys = keys
        this.default = default.toString()
    }

    constructor(vararg keys: String) : this(keys.toList())

    override fun get(pokemon: Pokemon) = pokemon.getFeature<FlagSpeciesFeature>(keys.first())

    override fun invoke(pokemon: Pokemon): FlagSpeciesFeature? {
        return get(pokemon)
            ?: when (default) {
                "random" -> FlagSpeciesFeature(keys.first(), Random.Default.nextBoolean())
                in setOf("true", "false") -> FlagSpeciesFeature(keys.first(), default.toBoolean())
                else -> null
            }
    }

    override fun invoke(nbt: NbtCompound): FlagSpeciesFeature? {
        return if (nbt.contains(keys.first())) {
            FlagSpeciesFeature(keys.first(), false).also { it.loadFromNBT(nbt) }
        } else null
    }

    override fun invoke(json: JsonObject): FlagSpeciesFeature? {
        return if (json.has(keys.first())) {
            FlagSpeciesFeature(keys.first(), false).also { it.loadFromJSON(json) }
        } else null
    }

    override fun fromString(value: String?): FlagSpeciesFeature? {
        val isWeirdValue = value != null && value !in examples()

        if (isWeirdValue) {
            return null
        }

        return if (value == null) {
            FlagSpeciesFeature(keys.first(), true)
        } else {
            FlagSpeciesFeature(keys.first(), value.toBoolean())
        }
    }

    override fun provide(pokemon: Pokemon): Set<String> {
        return if (isAspect && pokemon.getFeature<FlagSpeciesFeature>(keys.first())?.value == true) {
            setOf(keys.first())
        } else {
            emptySet()
        }
    }

    override fun provide(properties: PokemonProperties): Set<String> {
        return if (isAspect && properties.customProperties.filterIsInstance<FlagSpeciesFeature>().find { it.name == keys.first() }?.value == true) {
            setOf(keys.first())
        } else {
            emptySet()
        }
    }
}