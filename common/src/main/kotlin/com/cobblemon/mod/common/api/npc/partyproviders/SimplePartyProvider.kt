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
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.toProperties
import com.google.gson.JsonElement

/**
 * A basic party provider that just produces a [StaticNPCParty] based on a list of [PokemonProperties].
 *
 * @author Hiroku
 * @since August 19th, 2023
 */
class SimplePartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "simple"
    }

    @Transient
    override val type = TYPE

    val pokemon = mutableListOf<PokemonProperties>()

    override fun loadFromJSON(json: JsonElement) {
        val pokemonList = json.asJsonObject.getAsJsonArray("pokemon")
        pokemonList.forEach {
            val pokemon = if (it.isJsonPrimitive) {
                it.asString.toProperties()
            } else {
                val obj = it.asJsonObject
                val props = PokemonProperties()
                props.loadFromJSON(obj)
                props
            }

            this.pokemon.add(pokemon)
        }
    }

    override fun provide(npc: NPCEntity, level: Int): NPCParty {
        val pokemon = pokemon.map { it.copy().also { it.level = it.level ?: level }.create() }
        val party = NPCPartyStore(npc)
        pokemon.forEach(party::add)
        return StaticNPCParty(party)
    }
}