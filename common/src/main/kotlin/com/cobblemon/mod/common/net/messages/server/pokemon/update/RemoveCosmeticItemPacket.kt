/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

class RemoveCosmeticItemPacket(val pokemonId: UUID, val isParty: Boolean): NetworkPacket<RemoveCosmeticItemPacket> {
    companion object {
        val ID = cobblemonResource("remove_cosmetic_item")
        fun decode(buffer: RegistryFriendlyByteBuf) = RemoveCosmeticItemPacket(
            buffer.readUUID(), buffer.readBoolean()
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonId)
        buffer.writeBoolean(isParty)
    }
}