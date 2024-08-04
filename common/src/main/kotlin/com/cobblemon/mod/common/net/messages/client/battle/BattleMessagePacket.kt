/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization

/**
 * Sends messages to add to the battle message queue on the client.
 *
 * @author Hiroku
 * @since May 22nd, 2022
 */
class BattleMessagePacket(val messages: List<Component>) : NetworkPacket<BattleMessagePacket> {

    override val id = ID

    constructor(vararg messages: Component): this(messages.toList())

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(this.messages) { pb, value -> ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, value) }
    }

    companion object {
        val ID = cobblemonResource("battle_message")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleMessagePacket(buffer.readList { ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer) })
    }
}