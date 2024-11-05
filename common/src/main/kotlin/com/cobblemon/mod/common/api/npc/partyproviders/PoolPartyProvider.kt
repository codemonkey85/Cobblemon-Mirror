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
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.weightedSelection
import com.cobblemon.mod.common.util.withQueryValue
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * A provider of a party for battling the NPC. It generates a party using a more complex process
 * that takes an input level and makes a randomized selection of Pokémon and movesets from a pool.
 *
 * The party can be static or dynamic based on [isStatic]. If it is dynamic, the pool will be used
 * to generate a party whenever the NPC is challenged and AI that depends on a reliable party will
 * not run.
 *
 * Not an actual pool party. That's for v2.0.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
class PoolPartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "pool"
    }

    @Transient
    override val type = TYPE

    override var isStatic: Boolean = false
    var minPokemon: Expression = "1".asExpression()
    var maxPokemon: Expression = "6".asExpression()
    var pool = mutableListOf<DynamicPokemon>()

    override fun loadFromJSON(json: JsonElement) {
        json as JsonObject
        this.minPokemon = json.getAsJsonPrimitive("minPokemon").asString?.asExpression() ?: "1".asExpression()
        this.maxPokemon = json.getAsJsonPrimitive("maxPokemon").asString?.asExpression() ?: "6".asExpression()
        this.pool = json.getAsJsonArray("pool").map {
            if (it is JsonPrimitive) {
                return@map DynamicPokemon(
                    PokemonProperties.parse(it.asString),
                    0..0,
                    0..100,
                    "1".asExpression(),
                    "1".asExpression()
                )
            }

            it as JsonObject
            DynamicPokemon(
                PokemonProperties.parse(it.getAsJsonPrimitive("pokemon").asString),
                it.getAsJsonPrimitive("levelVariation")?.asString?.split("-")?.let { it[0].toInt()..it[1].toInt() } ?: 0..0,
                it.getAsJsonPrimitive("npcLevels")?.asString?.split("-")?.let { it[0].toInt()..it[1].toInt() } ?: 0..100,
                it.getAsJsonPrimitive("selectableTimes")?.asString?.asExpression() ?: "1".asExpression(),
                it.getAsJsonPrimitive("weight")?.asString?.asExpression() ?: "1".asExpression()
            )
        }.toMutableList()
        isStatic = json.getAsJsonPrimitive("isStatic").asBoolean
    }

    class DynamicPokemon(
        val pokemon: PokemonProperties,
        val levelVariation: IntRange,
        val npcLevels: IntRange,
        val selectableTimes: Expression,
        val weight: Expression = "1".asExpression()
    ) {
        fun getWeight(runtime: MoLangRuntime): Float {
            return runtime.resolveFloat(weight)
        }

        fun hasWeight(runtime: MoLangRuntime): Boolean {
            return runtime.resolveFloat(weight) != 0F
        }
    }

    fun formulateParty(npc: NPCEntity, level: Int, party: NPCPartyStore) {
        val runtime = MoLangRuntime().setup().withQueryValue("npc", npc.struct)
        val minPokemon = runtime.resolveInt(this.minPokemon)
        val maxPokemon = runtime.resolveInt(this.maxPokemon)
        var desiredPokemonCount = (minPokemon..maxPokemon).random()

        val workingPool = pool.filter { level in it.npcLevels }.toMutableList()
        val useCounts = mutableMapOf<DynamicPokemon, Int>()

        while (desiredPokemonCount > 0 && workingPool.filter { it.hasWeight(runtime) }.isNotEmpty()) {
            val selected = workingPool.filter { it.getWeight(runtime) == -1F }.randomOrNull()
                ?: workingPool.weightedSelection { it.getWeight(runtime) }
                ?: break
            useCounts[selected] = useCounts.getOrDefault(selected, 0) + 1
            val allowedSelections = runtime.resolveInt(selected.selectableTimes)
            if (useCounts[selected]!! >= allowedSelections) {
                workingPool.remove(selected)
            }
            desiredPokemonCount--

            // If the Pokémon's props specifies a level then use that, otherwise choose a random level within the range
            val randomLevel = (level + selected.levelVariation.random())
            val instance = selected.pokemon.copy().also { it.level = it.level ?: randomLevel }.create()
            party.add(instance)
        }
    }

    override fun provide(npc: NPCEntity, level: Int): NPCPartyStore {
        val party = NPCPartyStore(npc)
        formulateParty(npc, level, party)
        return party
    }
}