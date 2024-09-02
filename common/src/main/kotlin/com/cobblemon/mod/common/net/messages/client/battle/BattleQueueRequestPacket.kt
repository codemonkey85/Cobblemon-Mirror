/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ShowdownActionRequest
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Informs the client about the specific battle request that will be made of them at the next upkeep
 * or turn transition. The request isn't immediately displayed, request instructions come significantly
 * before the showdown request that indicates that a choice must be made.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleQueueRequestHandler].
 *
 * @author Hiroku
 * @since May 22nd, 2022
 */
class BattleQueueRequestPacket(val request: ShowdownActionRequest): NetworkPacket<BattleQueueRequestPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        request.saveToBuffer(buffer)
    }
    companion object {
        val ID = cobblemonResource("battle_queue_request")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleQueueRequestPacket(ShowdownActionRequest().loadFromBuffer(buffer))
    }
}