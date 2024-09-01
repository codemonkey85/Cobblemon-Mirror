/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.storage.BenchMoveHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Tells the server to exchange a current move with a benched move in the specified Pokémon's
 * moveset. It can be used for PC and party Pokémon.
 *
 * It should probably be split into two packets for which store type it's targeting, or include the store
 * position in an abstract way so that the PC case doesn't have to scavenge through the entire PC.
 *
 * Handled by [BenchMoveHandler].
 *
 * @author Hiroku
 * @since April 18th, 2022
 */
class BenchMovePacket(val isParty: Boolean, val uuid: UUID, val oldMove: MoveTemplate?, val newMove: MoveTemplate?) : NetworkPacket<BenchMovePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(isParty)
        buffer.writeUUID(uuid)
        buffer.writeBoolean(oldMove != null)
        if (oldMove != null) {
            buffer.writeString(oldMove.name)
        }
        buffer.writeBoolean( newMove != null)
        if (newMove != null) {
            buffer.writeString(newMove.name)
        }
    }

    companion object {
        val ID = cobblemonResource("bench_move")
        fun decode(buffer: RegistryFriendlyByteBuf): BenchMovePacket {
            val isParty = buffer.readBoolean()
            val uuid = buffer.readUUID()
            val oldMoveName = if(buffer.readBoolean()) buffer.readString() else null
            val oldMove = if (oldMoveName?.isNotEmpty() == true) Moves.getByName(oldMoveName) else null
            val newMoveName = if(buffer.readBoolean()) buffer.readString() else null
            val newMove = if (newMoveName?.isNotEmpty() == true) Moves.getByName(newMoveName) else null
            return BenchMovePacket(isParty, uuid, oldMove, newMove)
        }
    }
}