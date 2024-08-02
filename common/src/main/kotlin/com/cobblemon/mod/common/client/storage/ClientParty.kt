/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.storage

import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID
class ClientParty(uuid: UUID, slots: Int) : ClientStorage<PartyPosition>(uuid), Iterable<Pokemon?> {
    val slots = MutableList<Pokemon?>(slots) { null }

    override fun iterator() = slots.iterator()
    override fun findByUUID(uuid: UUID) = slots.find { it?.uuid == uuid }
    override fun set(position: PartyPosition, pokemon: Pokemon?) {
        if (position.slot >= slots.size) {
            return
        }

        slots[position.slot] = pokemon
    }

    fun get(slot: Int) = get(PartyPosition(slot))
    override fun get(position: PartyPosition): Pokemon? {
        if (position.slot >= slots.size || position.slot == -1) {
            return null
        }

        return slots[position.slot]
    }

    fun isEmpty() = (slots.size == 0)

    fun getPosition(pokemonID: UUID) = slots.indexOfFirst { it?.uuid == pokemonID }
    override fun getPosition(pokemon: Pokemon): PartyPosition? {
        for (slotNumber in slots.indices) {
            if (slots[slotNumber] == pokemon) {
                return PartyPosition(slotNumber)
            }
        }
        return null
    }
}