/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.weightedSelection
import com.cobblemon.mod.common.util.withQueryValue
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * A provider of a party for battling the NPC. It generates a [StaticNPCParty] but using a more complex process
 * that takes an input level and makes a randomized selection of Pokémon and movesets from a pool.
 *
 * Not an actual pool party. That's for v2.0.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
class StaticPoolPartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "static_pool"
    }

    class DynamicPokemon(
        val pokemon: PokemonProperties,
        val levelVariation: IntRange,
        val npcLevels: IntRange,
        val selectableTimes: Expression,
        val weight: Float = 1F
    )

    override val type = TYPE
    var minPokemon: Expression = "1".asExpression()
    var maxPokemon: Expression = "6".asExpression()
    var pool = mutableListOf<DynamicPokemon>()
    var static = true // TODO implement

    override fun loadFromJSON(json: JsonElement) {
        json as JsonObject
        this.minPokemon = json.getAsJsonPrimitive("minPokemon").asString?.asExpression() ?: "1".asExpression()
        this.maxPokemon = json.getAsJsonPrimitive("maxPokemon").asString?.asExpression() ?: "6".asExpression()
        this.pool = json.getAsJsonArray("pool").map {
            it as JsonObject
            DynamicPokemon(
                PokemonProperties.parse(it.getAsJsonPrimitive("pokemon").asString),
                it.getAsJsonPrimitive("levelVariation").asString?.split("..")?.let { it[0].toInt()..it[1].toInt() } ?: 0..0,
                it.getAsJsonPrimitive("npcLevels").asString?.split("..")?.let { it[0].toInt()..it[1].toInt() } ?: 0..100,
                it.getAsJsonPrimitive("selectableTimes").asString?.asExpression() ?: "1".asExpression(),
                it.getAsJsonPrimitive("weight").asFloat
            )
        }.toMutableList()
        static = json.getAsJsonPrimitive("static").asBoolean
    }

    fun formulateParty(npc: NPCEntity, level: Int, party: NPCPartyStore) {
        val runtime = MoLangRuntime().setup().withQueryValue("npc", npc.struct)
        val minPokemon = runtime.resolveInt(this.minPokemon)
        val maxPokemon = runtime.resolveInt(this.maxPokemon)
        var desiredPokemonCount = (minPokemon..maxPokemon).random()

        val workingPool = pool.filter { level in it.npcLevels }.toMutableList()
        val useCounts = mutableMapOf<DynamicPokemon, Int>()

        fun composePokemon(pokemon: DynamicPokemon): Pokemon {
            // If the Pokémon's props specifies a level then use that, otherwise choose a random level within the range
            val randomLevel = (level + pokemon.levelVariation.random())
            return pokemon.pokemon.copy().also { it.level = it.level ?: randomLevel }.create()
        }

        while (desiredPokemonCount > 0 && workingPool.isNotEmpty()) {
            val selected = workingPool.weightedSelection(DynamicPokemon::weight) ?: break
            useCounts[selected] = useCounts.getOrDefault(selected, 0) + 1
            val allowedSelections = runtime.resolveInt(selected.selectableTimes)
            if (useCounts[selected]!! >= allowedSelections) {
                workingPool.remove(selected)
            }
            desiredPokemonCount--
            party.add(composePokemon(selected))
        }
    }

    override fun provide(npc: NPCEntity, level: Int): NPCParty {
//        if (static) {
            val party = NPCPartyStore(npc)
            formulateParty(npc, level, party)
            return StaticNPCParty(party)
//        } else { TODO figure out dynamic saving and loading - we saving the entire logic in? Lose referential integrity?
//            return DynamicNPCParty(this, npc, level)
//        }

    }

}