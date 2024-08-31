/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.berry.adapter.SpawnConditionAdapter
import com.cobblemon.mod.common.api.berry.spawncondition.AllBiomeCondition
import com.cobblemon.mod.common.api.berry.spawncondition.BerrySpawnCondition
import com.cobblemon.mod.common.api.berry.spawncondition.PreferredBiomeCondition
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * The Cobblemon implementation of [SpawnConditionAdapter].
 *
 * @author Licious
 * @since December 2nd, 2022
 */
object CobblemonBerrySpawnConditionAdapter : SpawnConditionAdapter{
    private const val VARIANT = "variant"
    private val types = hashMapOf<String, KClass<out BerrySpawnCondition>>()

    init {
        this.register(PreferredBiomeCondition::class, PreferredBiomeCondition.ID)
        this.register(AllBiomeCondition::class, AllBiomeCondition.ID)
    }
    override fun register(type: KClass<out BerrySpawnCondition>, identifier: ResourceLocation) {
        val existing = this.types.put(identifier.toString(), type)
        if (existing != null) {
            Cobblemon.LOGGER.debug("Replaced {} under ID {} with {} in the {}", existing::class.qualifiedName, identifier.toString(), type.qualifiedName, this::class.qualifiedName)
        }
    }

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): BerrySpawnCondition {
        val json = jElement.asJsonObject
        val variant = json.get(VARIANT).asString.lowercase()
        val registeredType = this.types[variant] ?: throw IllegalArgumentException("Cannot resolve type for variant $variant")
        return context.deserialize(json, registeredType.java)
    }

    override fun serialize(spawnCondition: BerrySpawnCondition, type: Type, context: JsonSerializationContext): JsonElement {
        val json = context.serialize(spawnCondition).asJsonObject
        val variant = this.types.entries.find { it.value == spawnCondition::class }?.key ?: throw IllegalArgumentException("Cannot resolve variant for type ${spawnCondition::class.qualifiedName}")
        json.addProperty(VARIANT, variant)
        return json
    }
}
