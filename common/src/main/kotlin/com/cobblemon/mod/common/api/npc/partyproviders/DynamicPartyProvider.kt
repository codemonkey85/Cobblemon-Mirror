/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.google.gson.JsonElement
import net.minecraft.resources.ResourceLocation

/**
 * A provider of a party for battling the NPC. It generates a [StaticNPCParty] but using a more complex process
 * that takes an input level and makes a randomized selection of Pokémon and movesets.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
class DynamicPartyProvider : NPCPartyProvider {
    companion object {
        val TYPE = "dynamic"
    }

    class DynamicPool {
        val id = ResourceLocation.parse("cobblemon:pool")
        val pokemon: MutableList<DynamicPokemon> = mutableListOf()
    }


    class DynamicPokemon(
        val pokemon: PokemonProperties,
        val possibleLevels: IntRange,
        val selectableTimes: IntRange,
        val guaranteed: Boolean = false
    )

    override val type = "dynamic"

    override fun loadFromJSON(json: JsonElement) {
        TODO("Not yet implemented")
    }

    override fun provide(npc: NPCEntity, level: Int): NPCParty {
        TODO("Not yet implemented")
    }

}