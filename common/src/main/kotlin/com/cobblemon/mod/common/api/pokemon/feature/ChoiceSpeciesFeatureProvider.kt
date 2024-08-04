/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.client.gui.summary.featurerenderers.SummarySpeciesFeatureRenderer
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.substitute
import com.cobblemon.mod.common.util.writeString
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A [SpeciesFeatureProvider] which is a string value selected from a fixed list of choices. Parameters exist
 * to change default behaviour, aspects, and the available choices. Choices must be lowercase.
 *
 * @author Hiroku
 * @since November 30th, 2022
 */
open class ChoiceSpeciesFeatureProvider(
    override var keys: List<String>,
    var default: String? = null,
    var choices: List<String> = listOf(),
    var isAspect: Boolean = true,
    var aspectFormat: String = "{{choice}}"
) : SynchronizedSpeciesFeatureProvider<StringSpeciesFeature>, CustomPokemonPropertyType<StringSpeciesFeature>, AspectProvider {
    override var needsKey = true
    override var visible = false
    fun getAspect(feature: StringSpeciesFeature) = aspectFormat.substitute("choice", feature.value)

    override fun saveToBuffer(buffer: RegistryFriendlyByteBuf, toClient: Boolean) {
        buffer.writeCollection(keys) { _, value -> buffer.writeString(value) }
        buffer.writeNullable(default) { _, value -> buffer.writeString(value) }
        buffer.writeCollection(choices) { _, value -> buffer.writeString(value) }
        buffer.writeBoolean(isAspect)
        buffer.writeString(aspectFormat)
        buffer.writeBoolean(needsKey)
    }

    override fun loadFromBuffer(buffer: RegistryFriendlyByteBuf) {
        keys = buffer.readList { buffer.readString() }
        default = buffer.readNullable { buffer.readString() }
        choices = buffer.readList { buffer.readString() }
        isAspect = buffer.readBoolean()
        aspectFormat = buffer.readString()
        needsKey = buffer.readBoolean()
    }

    override fun getRenderer(pokemon: Pokemon): SummarySpeciesFeatureRenderer<StringSpeciesFeature>? {
        return null
    }

    override fun invoke(buffer: RegistryFriendlyByteBuf, name: String): StringSpeciesFeature? {
        return if (name in keys) {
            StringSpeciesFeature(name, "").also { it.loadFromBuffer(buffer) }
        } else {
            null
        }
    }

    fun getAllAspects(): MutableList<String> {
        val aspects = choices.toMutableList()
        choices.forEach {
            aspects[choices.indexOf(it)] = (aspectFormat.substitute("choice", it))
        }
        return aspects
    }

    override fun examples() = choices

    internal constructor(): this(emptyList())

    override fun get(pokemon: Pokemon) = pokemon.getFeature<StringSpeciesFeature>(keys.first())

    override fun invoke(pokemon: Pokemon): StringSpeciesFeature? {
        val existing = get(pokemon)
        return if (existing != null && existing.value in choices) {
            existing
        } else {
            val value = if (default in choices) {
                default!!
            } else if (default == "random") {
                // If it's mandatory, but they provided no value and no default, give it a random value.
                choices.randomOrNull() ?: throw IllegalStateException("The 'choices' list is empty for species feature provider: ${keys.joinToString()}")
            } else {
                return null
            }

            fromString(value)
        }
    }

    override fun invoke(nbt: CompoundTag): StringSpeciesFeature? {
        return if (nbt.contains(keys.first())) {
            StringSpeciesFeature(keys.first(), "").also { it.loadFromNBT(nbt) }
        } else null
    }

    override fun invoke(json: JsonObject): StringSpeciesFeature? {
        return if (json.has(keys.first())) {
            StringSpeciesFeature(keys.first(), "").also { it.loadFromJSON(json) }
        } else null
    }

    override fun fromString(value: String?): StringSpeciesFeature? {
        val lower = value?.lowercase()
        if (lower == null || lower !in choices) {
            return null
        }

        return StringSpeciesFeature(keys.first(), lower)
    }

    override fun provide(pokemon: Pokemon): Set<String> {
        return if (isAspect) {
            get(pokemon)?.let { setOf(getAspect(it)) } ?: emptySet()
        } else {
            emptySet()
        }
    }

    override fun provide(properties: PokemonProperties): Set<String> {
        return if (isAspect) {
            val feature = properties.customProperties.filterIsInstance<StringSpeciesFeature>().find { it.name == keys.first() }
            if (feature != null) {
                setOf(getAspect(feature))
            } else {
                emptySet()
            }
        } else {
            emptySet()
        }
    }
}