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
 * Collection of all AI properties defineable at the species level of a Pokémon.
 *
 * @author Hiroku
 * @since July 15th, 2022
 */
data class PokemonBehaviour(
    val resting: RestBehaviour = RestBehaviour(),
    var moving: MoveBehaviour = MoveBehaviour(),
    val idle: IdleBehaviour = IdleBehaviour(),
) {
    companion object {
        @JvmStatic
        val CODEC: Codec<PokemonBehaviour> = RecordCodecBuilder.create { instance ->
            instance.group(
                RestBehaviour.CODEC.optionalFieldOf("resting", RestBehaviour()).forGetter(PokemonBehaviour::resting),
                MoveBehaviour.CODEC.optionalFieldOf("moving", MoveBehaviour()).forGetter(PokemonBehaviour::moving),
                IdleBehaviour.CODEC.optionalFieldOf("idle", IdleBehaviour()).forGetter(PokemonBehaviour::idle),
            ).apply(instance, ::PokemonBehaviour)
        }
    }
}