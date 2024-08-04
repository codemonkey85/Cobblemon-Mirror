/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to change the status of a Pokémon in battle, such as paralysis or sleep.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattlePersistentStatusHandler].
 *
 * @author Hiroku
 * @since November 5th, 2022
 */
class BattlePersistentStatusPacket(val pnx: String, val status: PersistentStatus?) : NetworkPacket<BattlePersistentStatusPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(pnx)
        buffer.writeNullable(status) { buf, value -> buf.writeIdentifier(value.name)}
    }

    companion object {
        val ID = cobblemonResource("battle_persistent_status")
        fun decode(buffer: RegistryFriendlyByteBuf): BattlePersistentStatusPacket {
            val pnx = buffer.readString()
            val statusId = buffer.readNullable { it.readIdentifier() } ?: return BattlePersistentStatusPacket(pnx, null)
            val status = Statuses.getStatus(statusId) as? PersistentStatus
            return BattlePersistentStatusPacket(pnx, status)
        }
    }
}