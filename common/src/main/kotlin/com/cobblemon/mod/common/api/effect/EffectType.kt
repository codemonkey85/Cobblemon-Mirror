/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.effect

import com.cobblemon.mod.common.api.effect.types.ElementalTypeEffect
import com.cobblemon.mod.common.api.effect.types.ShowdownConditionEffect
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

fun interface EffectType<T : Effect> {

    fun codec(): MapCodec<T>

    companion object {

        internal val REGISTRY = MappedRegistry<EffectType<*>>(
            ResourceKey.createRegistryKey(cobblemonResource("effect_type")),
            Lifecycle.stable()
        )

        @JvmStatic
        val SHOWDOWN_CONDITION = this.register(cobblemonResource("showdown_condition"), ShowdownConditionEffect.CODEC)

        @JvmStatic
        val ELEMENTAL_TYPE = this.register(cobblemonResource("elemental_type"), ElementalTypeEffect.CODEC)

        // TODO: Add specific move, ability and status as those get registry

        fun <T : Effect> register(id: ResourceLocation, codec: MapCodec<T>): EffectType<T> {
            return Registry.register(REGISTRY, id, EffectType { codec })
        }

    }

}