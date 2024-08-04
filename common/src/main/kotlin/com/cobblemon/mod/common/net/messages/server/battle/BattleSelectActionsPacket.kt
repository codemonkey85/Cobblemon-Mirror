/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

class BattleSelectActionsPacket(val battleId: UUID, val showdownActionResponses: List<ShowdownActionResponse>) : NetworkPacket<BattleSelectActionsPacket> {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battleId)
        buffer.writeSizedInt(IntSize.U_BYTE, showdownActionResponses.size)
        showdownActionResponses.forEach { it.saveToBuffer(buffer) }
    }

    companion object {
        val ID = cobblemonResource("battle_select_actions")
        fun decode(buffer: RegistryFriendlyByteBuf): BattleSelectActionsPacket {
            val battleId = buffer.readUUID()
            val responses = mutableListOf<ShowdownActionResponse>()
            repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
                responses.add(ShowdownActionResponse.loadFromBuffer(buffer))
            }
            return BattleSelectActionsPacket(battleId, responses)
        }
    }

}