/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.effect.adapter

import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffectRegistry
import com.cobblemon.mod.common.pokemon.effects.PotionBaseEffect
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object ShoulderEffectAdapter: JsonDeserializer<ShoulderEffect> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShoulderEffect {
        val (typeId, obj) = if (json.isJsonPrimitive) {
            json.asString to JsonObject()
        } else {
            json.asJsonObject.get("type").asString to json.asJsonObject
        }
        val effect = ShoulderEffectRegistry.get(typeId) ?: run {
            try {
                val effectId = ResourceLocation.parse(typeId.replace("-", "_").replace("slow_fall", "slow_falling"))
                val registry = BuiltInRegistries.MOB_EFFECT
                val effect = registry.get(effectId)
                if (effect != null) {
                    return PotionBaseEffect(effect, 0, true, false, false)
                }
            } catch (_: Exception) {}

            throw IllegalArgumentException("Cannot find shoulder effect with type '$typeId'")
        }
        return context.deserialize(obj, effect)
    }
}