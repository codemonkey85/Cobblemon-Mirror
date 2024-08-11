/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.lighthing

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.StringRepresentable

/**
 * Represents light emitting properties of a species/form.
 * This has no use in the base mod and is instead used for dynamic lighting mod compatibility.
 *
 * @property lightLevel The light level emitted.
 * @property liquidGlowMode The [LiquidGlowMode] for this effect.
 */
data class LightingData(val lightLevel: Int, val liquidGlowMode: LiquidGlowMode) {

    /**
     * Represents if a [LightingData] is applied while in land or underwater
     *
     * @property glowsInLand If this allows activation while on land.
     * @property glowsUnderwater If this allows activation while underwater.
     */
    @Suppress("unused")
    enum class LiquidGlowMode(val glowsInLand: Boolean, val glowsUnderwater: Boolean) : StringRepresentable {

        LAND(true, false),
        UNDERWATER(false, true),
        BOTH(true, true);

        override fun getSerializedName(): String = this.name

        companion object {
            @JvmStatic
            val CODEC: Codec<LiquidGlowMode> = StringRepresentable.fromEnum(LiquidGlowMode::values)
        }

    }

    companion object {
        @JvmStatic
        val CODEC: Codec<LightingData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(0, 15).fieldOf("lightLevel").forGetter(LightingData::lightLevel),
                LiquidGlowMode.CODEC.fieldOf("liquidGlowMode").forGetter(LightingData::liquidGlowMode),
            ).apply(instance, ::LightingData)
        }
    }

}
