/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.evolution.CobblemonEvolutionDisplay
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * Represents an evolution of a [Pokemon], this is the client side counterpart of [Evolution].
 * This has no attachments to any data regarding the evolution itself and only serves for display purposes and basic communication.
 *
 * @author Licious
 * @since April 28th, 2022
 */
interface EvolutionDisplay : EvolutionLike {

    /**
     * The [Species] of the evolution result.
     */
    val species: Species

    /**
     * The aspects of the evolution result.
     * These are used by the client to resolve the entity it needs to display.
     */
    val aspects: Set<String>

    companion object {
        @JvmStatic
        val CODEC: Codec<EvolutionDisplay> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter(EvolutionDisplay::id),
                Species.BY_IDENTIFIER_CODEC.fieldOf("species").forGetter(EvolutionDisplay::species),
                Codec.list(Codec.STRING).fieldOf("aspects").forGetter { it.aspects.toList() }
            ).apply(instance) { id, species, aspects -> CobblemonEvolutionDisplay(id, species, aspects.toSet()) }
        }
    }

}