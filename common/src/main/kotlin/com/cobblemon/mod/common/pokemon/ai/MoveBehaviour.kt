/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * Behavioural properties relating to a Pokémon's ability to look and move.
 *
 * @author Hiroku
 * @since July 30th, 2022
 */
data class MoveBehaviour(
    val walk: WalkBehaviour = WalkBehaviour(),
    val swim: SwimBehaviour = SwimBehaviour(),
    val fly: FlyBehaviour = FlyBehaviour(),
    val stepHeight: Float = 0.6F,
    val wanderChance: Int = 120,
    val wanderSpeed: Double = 1.0,
    val canLook: Boolean = true,
    val looksAtEntities: Boolean = true
) {
    companion object {
        @JvmStatic
        val CODEC: Codec<MoveBehaviour> = RecordCodecBuilder.create { instance ->
            instance.group(
                WalkBehaviour.CODEC.optionalFieldOf("walk", WalkBehaviour()).forGetter(MoveBehaviour::walk),
                SwimBehaviour.CODEC.optionalFieldOf("swim", SwimBehaviour()).forGetter(MoveBehaviour::swim),
                FlyBehaviour.CODEC.optionalFieldOf("fly", FlyBehaviour()).forGetter(MoveBehaviour::fly),
                Codec.FLOAT.optionalFieldOf("stepHeight", 0.6F).forGetter(MoveBehaviour::stepHeight),
                Codec.INT.optionalFieldOf("wanderChance", 120).forGetter(MoveBehaviour::wanderChance),
                Codec.DOUBLE.optionalFieldOf("wanderSpeed", 1.0).forGetter(MoveBehaviour::wanderSpeed),
                Codec.BOOL.optionalFieldOf("canLook", true).forGetter(MoveBehaviour::canLook),
                Codec.BOOL.optionalFieldOf("looksAtEntities", true).forGetter(MoveBehaviour::looksAtEntities),
            ).apply(instance, ::MoveBehaviour)
        }
    }
}