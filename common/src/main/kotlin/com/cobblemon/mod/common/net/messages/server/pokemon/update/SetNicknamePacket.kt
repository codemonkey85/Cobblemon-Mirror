/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.pokemon.update.SetNicknameHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to the server to indicate a player wants to change the nickname of a Pokémon. If the [nickname]
 * value is null then they are trying to remove the nickname.
 *
 * Handled by [SetNicknameHandler].
 *
 * @author selfdot
 * @since March 29th, 2023
 */
class SetNicknamePacket(val pokemonUUID: UUID, val isParty: Boolean, val nickname: String?) : NetworkPacket<SetNicknamePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonUUID)
        buffer.writeBoolean(isParty)
        buffer.writeNullable(nickname) { _, v -> buffer.writeString(v) }
    }
    companion object {
        val ID = cobblemonResource("set_nickname")
        fun decode(buffer: RegistryFriendlyByteBuf) = SetNicknamePacket(
            buffer.readUUID(), buffer.readBoolean(), buffer.readNullable { buffer.readString() }
        )
    }
}