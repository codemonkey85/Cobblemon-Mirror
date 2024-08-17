/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class SpectateBattlePacket(val targetedEntityId: UUID) : NetworkPacket<SpectateBattlePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(targetedEntityId)
    }

    companion object {
        val ID = cobblemonResource("battle_spectate")
        fun decode(buffer: RegistryFriendlyByteBuf) = SpectateBattlePacket(buffer.readUUID())
    }
}