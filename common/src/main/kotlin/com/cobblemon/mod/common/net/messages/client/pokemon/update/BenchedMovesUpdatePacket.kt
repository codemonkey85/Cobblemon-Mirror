/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.moves.BenchedMoves
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class BenchedMovesUpdatePacket(pokemon: () -> Pokemon, value: BenchedMoves): SingleUpdatePacket<BenchedMoves, BenchedMovesUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        this.value.saveToBuffer(buffer)
    }

    override fun set(pokemon: Pokemon, value: BenchedMoves) {
        pokemon.benchedMoves.copyFrom(value)
    }

    companion object {
        val ID = cobblemonResource("benched_moves_update")
        fun decode(buffer: RegistryFriendlyByteBuf): BenchedMovesUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val benchedMoves = BenchedMoves().apply { loadFromBuffer(buffer) }
            return BenchedMovesUpdatePacket(pokemon, benchedMoves)
        }
    }
}