/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.effect.types

import com.cobblemon.mod.common.api.effect.Effect
import com.cobblemon.mod.common.api.effect.EffectType
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceKey

class ElementalTypeEffect(private val elementalTypeKey: ResourceKey<ElementalType>) : Effect() {
    override fun type(): EffectType<*> = EffectType.ELEMENTAL_TYPE

    override fun showdownId(): String = this.elementalType().showdownId()

    fun elementalType(): ElementalType = CobblemonRegistries.ELEMENTAL_TYPE.get(this.elementalTypeKey)
        ?: throw IllegalArgumentException("Cannot resolve ${this.elementalTypeKey} in ${CobblemonRegistries.ELEMENTAL_TYPE}")

    companion object {

        @JvmStatic
        val CODEC: MapCodec<ElementalTypeEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceKey.codec(CobblemonRegistries.ELEMENTAL_TYPE_KEY)
                    .fieldOf("typeId")
                    .forGetter(ElementalTypeEffect::elementalTypeKey)
            ).apply(instance, ::ElementalTypeEffect)
        }

    }
}