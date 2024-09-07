/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Updates the client about an [ActiveBattlePokemon] that was hidden by an illusion and has just been revealed during a battle.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleReplacePokemonHandler].
 *
 * @param realPokemon The
 * @author Segfault Guy
 * @since March 30th, 2024
 */
class BattleReplacePokemonPacket(val pnx: String, val realPokemon: BattleInitializePacket.ActiveBattlePokemonDTO, val isAlly: Boolean) : NetworkPacket<BattleReplacePokemonPacket> {

    override val id = ID

    constructor(pnx: String, realPokemon: BattlePokemon, isAlly: Boolean) :
        this(pnx, BattleInitializePacket.ActiveBattlePokemonDTO.fromPokemon(realPokemon, isAlly), isAlly)

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(pnx)
        realPokemon.saveToBuffer(buffer)
        buffer.writeBoolean(isAlly)
    }

    companion object {
        val ID = cobblemonResource("battle_replace_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleReplacePokemonPacket(buffer.readString(), BattleInitializePacket.ActiveBattlePokemonDTO.loadFromBuffer(buffer), buffer.readBoolean())
    }
}