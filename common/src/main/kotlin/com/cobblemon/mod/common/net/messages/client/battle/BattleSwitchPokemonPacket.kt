/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf


/**
 * Informs the client about a switch occurring in the battle.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleSwitchPokemonHandler].
 *
 * @author Hiroku
 * @since June 6th, 2022
 */
class BattleSwitchPokemonPacket(val pnx: String, val newPokemon: BattleInitializePacket.ActiveBattlePokemonDTO, val isAlly: Boolean) : NetworkPacket<BattleSwitchPokemonPacket> {

    override val id = ID

    constructor(pnx: String, newPokemon: BattlePokemon, isAlly: Boolean, illusion: BattlePokemon?) :
        this(pnx, BattleInitializePacket.ActiveBattlePokemonDTO.fromPokemon(newPokemon, isAlly, illusion), isAlly)

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(pnx)
        newPokemon.saveToBuffer(buffer)
        buffer.writeBoolean(isAlly)
    }

    companion object {
        val ID = cobblemonResource("battle_switch_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleSwitchPokemonPacket(buffer.readString(), BattleInitializePacket.ActiveBattlePokemonDTO.loadFromBuffer(buffer), buffer.readBoolean())
    }
}