/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * An [EvolutionRequirement] for when the party needs to either contain or not a specific match for [target] based on the [contains] property.
 *
 * @property target The matcher for the party members.
 * @property contains If this requirement will need the [target] to be present or not.
 * @author Licious
 * @since March 21st, 2022
 */
class PartyMemberRequirement(val target: PokemonProperties, val contains: Boolean) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean {
        val party = pokemon.storeCoordinates.get()?.store as? PartyStore ?: return false
        val has = party.any { member -> member.uuid != pokemon.uuid && this.target.matches(member) }
        return this.contains == has
    }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.PARTY_MEMBER

    companion object {
        @JvmStatic
        val CODEC: MapCodec<PartyMemberRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PokemonProperties.CODEC.fieldOf("target").forGetter(PartyMemberRequirement::target),
                Codec.BOOL.fieldOf("contains").forGetter(PartyMemberRequirement::contains),
            ).apply(instance, ::PartyMemberRequirement)
        }
    }

}