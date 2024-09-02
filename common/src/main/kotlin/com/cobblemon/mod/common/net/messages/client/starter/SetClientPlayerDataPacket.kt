/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.starter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.player.PlayerData
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet to update the general player data on the client (which is just starter information).
 *
 * @author Hiroku
 * @since August 1st, 2022
 */
class SetClientPlayerDataPacket(val promptStarter: Boolean, val starterLocked: Boolean, val starterSelected: Boolean, val starterUUID: UUID?, val resetStarterPrompt: Boolean?) : NetworkPacket<SetClientPlayerDataPacket> {

    override val id = ID

    constructor(
        playerData: PlayerData,
        resetStarterPrompt: Boolean? = null
    ) : this(
        !playerData.starterPrompted || !Cobblemon.starterConfig.promptStarterOnceOnly,
        playerData.starterLocked,
        playerData.starterSelected,
        playerData.starterUUID,
        resetStarterPrompt
    )

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(promptStarter)
        buffer.writeBoolean(starterLocked)
        buffer.writeBoolean(starterSelected)
        val starterUUID = starterUUID
        buffer.writeNullable(starterUUID) { pb, value -> pb.writeUUID(value) }
        val resetStarterPrompt = resetStarterPrompt
        buffer.writeNullable(resetStarterPrompt) { pb, value -> pb.writeBoolean(value) }
    }

    companion object {
        val ID = cobblemonResource("set_client_playerdata")
        fun decode(buffer: RegistryFriendlyByteBuf): SetClientPlayerDataPacket {
            val promptStarter = buffer.readBoolean()
            val starterLocked = buffer.readBoolean()
            val starterSelected = buffer.readBoolean()
            val starterUUID = buffer.readNullable { it.readUUID() }
            val resetStarterPrompt = buffer.readNullable { it.readBoolean() }
            return SetClientPlayerDataPacket(promptStarter, starterLocked, starterSelected, starterUUID, resetStarterPrompt)
        }
    }
}