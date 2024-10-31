/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf


/**
 * Gives the client the true details of their team in the battle. This is so that switch choices can be made with
 * full details.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleSetTeamPokemonHandler].
 *
 * @author Hiroku
 * @since June 6th, 2022
 */
class BattleSetTeamPokemonPacket(val team: List<Pokemon>) : NetworkPacket<BattleSetTeamPokemonPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(this.team) { _, pokemon -> Pokemon.S2C_CODEC.encode(buffer, pokemon) }
    }
    companion object {
        val ID = cobblemonResource("battle_set_team")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleSetTeamPokemonPacket(buffer.readList { _ -> Pokemon.S2C_CODEC.decode(buffer) })
    }
}