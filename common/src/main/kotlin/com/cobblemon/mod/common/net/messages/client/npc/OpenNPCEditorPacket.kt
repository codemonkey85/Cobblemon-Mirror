/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.npc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.npc.dto.NPCConfigurationDTO
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class OpenNPCEditorPacket(
    val npcId: Int,
    val dto: NPCConfigurationDTO
) : NetworkPacket<OpenNPCEditorPacket> {
    companion object {
        val ID = cobblemonResource("open_npc_editor")
        fun decode(buffer: RegistryFriendlyByteBuf) = OpenNPCEditorPacket(
            npcId = buffer.readInt(),
            dto = NPCConfigurationDTO().apply { decode(buffer) }
        )
    }

    override val id = ID

    constructor(npc: NPCEntity): this(npcId = npc.id, dto = NPCConfigurationDTO(npc))

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(npcId)
        dto.encode(buffer)
    }
}