/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Updates the client about an [ActiveBattlePokemon] that has changed due to transformation during a battle.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleTransformPokemonHandler].
 *
 * @author Segfault Guy
 * @since March 26th, 2024
 */
class BattleTransformPokemonPacket(val pnx: String, val updatedPokemon: BattleInitializePacket.ActiveBattlePokemonDTO, val isAlly: Boolean) : NetworkPacket<BattleTransformPokemonPacket> {

    override val id = ID

    // form changes
    constructor(pnx: String, updatedPokemon: BattlePokemon, isAlly: Boolean) :
        this(pnx, BattleInitializePacket.ActiveBattlePokemonDTO.fromPokemon(updatedPokemon, isAlly), isAlly)

    // transform
    constructor(pnx: String, updatedPokemon: BattlePokemon, mock: PokemonProperties, isAlly: Boolean) :
            this(pnx, BattleInitializePacket.ActiveBattlePokemonDTO.fromMock(updatedPokemon, isAlly, mock), isAlly)

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(pnx)
        updatedPokemon.saveToBuffer(buffer)
        buffer.writeBoolean(isAlly)
    }

    companion object {
        val ID = cobblemonResource("battle_transform_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleTransformPokemonPacket(buffer.readString(), BattleInitializePacket.ActiveBattlePokemonDTO.loadFromBuffer(buffer), buffer.readBoolean())
    }
}