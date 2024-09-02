/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.callback

import com.cobblemon.mod.common.api.callback.PartySelectPokemonDTO
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import java.util.*

/**
 * Packet send to the client to force them to open a party selection GUI.
 *
 * @author Hiroku
 * @since July 7th, 2023
 */
class OpenPartyCallbackPacket(
    val uuid: UUID,
    val title: MutableComponent,
//    val usePortraits: Boolean,
//    val animate: Boolean,
    val pokemon: List<PartySelectPokemonDTO>
) : NetworkPacket<OpenPartyCallbackPacket> {
    companion object {
        val ID = cobblemonResource("open_party_callback")
        fun decode(buffer: RegistryFriendlyByteBuf) = OpenPartyCallbackPacket(
            uuid = buffer.readUUID(),
            title = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer).copy(),
//            usePortraits = buffer.readBoolean(),
//            animate = buffer.readBoolean(),
            pokemon = buffer.readList { _ -> PartySelectPokemonDTO(buffer) }
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, title)
//        buffer.writeBoolean(usePortraits)
//        buffer.writeBoolean(animate)
        buffer.writeCollection(pokemon) { _, v -> v.writeToBuffer(buffer) }
    }
}