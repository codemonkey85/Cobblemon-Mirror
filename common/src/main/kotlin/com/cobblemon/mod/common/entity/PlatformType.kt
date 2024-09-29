/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import java.util.EnumSet

/**
 * The type of a platform. Used for battling on .
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
enum class PlatformType {
    NONE,
    WATER_XS,
    WATER_S,
    WATER_M,
    WATER_L,
    WATER_XL;


    companion object {
        val ALL_POSES = EnumSet.allOf(PlatformType::class.java)
        val WATER = EnumSet.of(WATER_XS, WATER_S, WATER_M, WATER_L, WATER_XL)

        fun GetModelWithTexture(type: PlatformType) : Pair<ResourceLocation, ResourceLocation> {
            return when (type) {
                WATER_XS -> Pair(cobblemonResource("water_platform_xs.geo"), cobblemonResource("textures/platforms/water_platform_xs.png"))
                WATER_S -> Pair(cobblemonResource("water_platform_s.geo"), cobblemonResource("textures/platforms/water_platform_s.png"))
                WATER_M -> Pair(cobblemonResource("water_platform_m.geo"), cobblemonResource("textures/platforms/water_platform_m.png"))
                WATER_L -> Pair(cobblemonResource("water_platform_l.geo"), cobblemonResource("textures/platforms/water_platform_l.png"))
                WATER_XL -> Pair(cobblemonResource("water_platform_xl.geo"), cobblemonResource("textures/platforms/water_platform_xl.png"))
                else -> Pair(cobblemonResource("water_platform_xs.geo"), cobblemonResource("textures/platforms/water_platform_xs"))
            }
        }

        fun GetPlatformTypeForPokemon(pokemon: Pokemon) : PlatformType {
            val width = pokemon.form.hitbox.width * pokemon.form.baseScale * 1.414 // approx sqrt(2*x^2)

            return if (width < 1.0) {
               WATER_XS
            } else if (width < 1.25) {
                WATER_S
            } else if (width < 2.5) {
                WATER_M
            } else if (width < 3.0) {
                WATER_L
            } else {
                WATER_XL
            }
        }
    }
}