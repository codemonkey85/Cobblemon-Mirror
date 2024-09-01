/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.javaType
import kotlin.reflect.jvm.isAccessible
import net.minecraft.server.packs.PackType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

internal object SpeciesAdditions : JsonDataRegistry<SpeciesAdditions.AdditionParameter> {

    override val id = cobblemonResource("species_additions")
    override val type = PackType.SERVER_DATA
    override val observable  = SimpleObservable<SpeciesAdditions>()
    override val gson: Gson = PokemonSpecies.gson.newBuilder().registerTypeAdapter(AdditionParameter::class.java, AdditionParameterAdapter).create()
    override val typeToken: TypeToken<AdditionParameter> = TypeToken.get(AdditionParameter::class.java)
    override val resourcePath: String = this.id.path

    override fun reload(data: Map<ResourceLocation, AdditionParameter>) {
        for ((identifier,parameter) in data) {
            val species = PokemonSpecies.getByIdentifier(parameter.targetIdentifier)
            if (species == null) {
                Cobblemon.LOGGER.warn("Cannot find species {} for addition {}, skipping", parameter.targetIdentifier.toString(), identifier.toString())
                continue
            }
            parameter.additions.forEach { addition ->
                try {
                    var value = addition.value
                    if (value is MutableCollection<*>) {
                        val existing = addition.property.getter.call(species) as MutableCollection<Any>
                        existing.addAll(value.filterNotNull())
                        value = existing
                    }
                    else if (value is MutableMap<*, *>) {
                        val existing = addition.property.getter.call(species) as MutableMap<Any, Any>
                        existing.putAll(value as MutableMap<Any, Any>)
                        value = existing
                    }
                    addition.property.setter.call(species, value)
                } catch (e: Exception) {
                    Cobblemon.LOGGER.error("Caught exception applying addition {} to {}", identifier.toString(), parameter.targetIdentifier.toString(), e)
                }
            }
        }
        Cobblemon.LOGGER.info("Finished additions")
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {}

    data class AdditionParameter(
        val targetIdentifier: ResourceLocation,
        val additions: Collection<Addition>
    )

    data class Addition(
        val property: KMutableProperty<*>,
        val value: Any
    )

    object AdditionParameterAdapter : JsonDeserializer<AdditionParameter> {

        private const val TARGET = "target"
        private val properties = hashMapOf<String, KMutableProperty<*>>()

        init {
            for (property in Species::class.declaredMemberProperties) {
                if (property.isLateinit || property !is KMutableProperty<*>) {
                    continue
                }
                if (!property.isAccessible) {
                    property.isAccessible = true
                }
                this.properties[property.name] = property
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): AdditionParameter {
            val jObject = element.asJsonObject
            val target = jObject.get(TARGET).asString.asIdentifierDefaultingNamespace()
            val additions = arrayListOf<Addition>()
            for ((key, jElement) in jObject.entrySet()) {
                if (key == TARGET || !this.properties.containsKey(key)) {
                    continue
                }
                val property = this.properties[key]!!
                val value = context.deserialize<Any>(jElement, property.returnType.javaType)
                additions += Addition(property, value)
            }
            return AdditionParameter(target, additions)
        }
    }

}