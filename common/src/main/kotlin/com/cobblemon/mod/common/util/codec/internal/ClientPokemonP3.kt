/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal

import com.cobblemon.mod.common.pokemon.OriginalTrainerType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.*

internal data class ClientPokemonP3(
    val originalTrainerType: OriginalTrainerType,
    val originalTrainer: Optional<String>,
    val originalTrainerName: Optional<String>,
    val aspects: Set<String>
) : Partial<Pokemon> {

    override fun into(other: Pokemon): Pokemon {
        other.originalTrainerType = this.originalTrainerType
        this.originalTrainer.ifPresent { other.originalTrainer = it }
        this.originalTrainerName.ifPresent { other.originalTrainerName = it }
        other.forcedAspects = this.aspects
        return other
    }

    companion object {
        internal val CODEC: MapCodec<ClientPokemonP3> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                OriginalTrainerType.CODEC.optionalFieldOf(DataKeys.POKEMON_ORIGINAL_TRAINER_TYPE, OriginalTrainerType.NONE).forGetter(ClientPokemonP3::originalTrainerType),
                Codec.STRING.optionalFieldOf(DataKeys.POKEMON_ORIGINAL_TRAINER).forGetter(ClientPokemonP3::originalTrainer),
                Codec.STRING.optionalFieldOf(DataKeys.POKEMON_ORIGINAL_TRAINER).forGetter(ClientPokemonP3::originalTrainerName),
                Codec.list(Codec.STRING).optionalFieldOf(DataKeys.POKEMON_FORCED_ASPECTS, emptyList()).xmap({ it.toSet() }, { it.toMutableList() }).forGetter(ClientPokemonP3::aspects),
            ).apply(instance, ::ClientPokemonP3)
        }

        internal fun from(pokemon: Pokemon): ClientPokemonP3 = ClientPokemonP3(
            pokemon.originalTrainerType,
            Optional.ofNullable(pokemon.originalTrainer),
            Optional.ofNullable(pokemon.originalTrainerName),
            pokemon.aspects + pokemon.forcedAspects
        )
    }

}