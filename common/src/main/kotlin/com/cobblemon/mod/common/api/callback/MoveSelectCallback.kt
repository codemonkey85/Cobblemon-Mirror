/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.callback

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.battles.InBattleMove
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.net.messages.client.callback.OpenMoveCallbackPacket
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import java.util.UUID
import net.minecraft.server.level.ServerPlayer

/**
 * Used for opening move selection screens for players and handling their choice. Currently
 * only supports up to 4 moves to select from.
 *
 * @author Hiroku
 * @since July 1st, 2023
 */
object MoveSelectCallbacks {
    val callbacks = mutableMapOf<UUID, MoveSelectCallback>()

    @JvmOverloads
    fun create(
        player: ServerPlayer,
        title: Component = "".text(),
        possibleMoves: List<MoveSelectDTO>,
        cancel: (ServerPlayer) -> Unit = {},
        handler: (ServerPlayer, index: Int, MoveSelectDTO) -> Unit
    ) {
        val callback = MoveSelectCallback(
            shownMoves = possibleMoves,
            cancel = cancel,
            handler = handler
        )

        callbacks[player.uuid] = callback

        player.sendPacket(OpenMoveCallbackPacket(callback.uuid, title.copy(), possibleMoves))
    }

    @JvmOverloads
    fun create(
        player: ServerPlayer,
        moves: List<Move>,
        canSelect: (Move) -> Boolean = { true },
        cancel: (ServerPlayer) -> Unit = {},
        handler: (Move) -> Unit
    ) = create(
        player = player,
        possibleMoves = moves.map { battleMove -> MoveSelectDTO(battleMove).also { it.enabled = canSelect(battleMove) } },
        cancel = cancel,
        handler = { _, index, _ -> handler(moves[index]) }
    )

    @JvmOverloads
    fun createBattleSelect(
        player: ServerPlayer,
        moves: List<InBattleMove>,
        canSelect: (InBattleMove) -> Boolean = { true },
        cancel: (ServerPlayer) -> Unit = {},
        handler: (InBattleMove) -> Unit
    ) = create(
        player = player,
        possibleMoves = moves.map { battleMove -> MoveSelectDTO(battleMove).also { it.enabled = canSelect(battleMove) } },
        cancel = cancel,
        handler = { _, index, _ -> handler(moves[index]) }
    )

    fun handleCancelled(player: ServerPlayer, uuid: UUID) {
        val callback = callbacks[player.uuid] ?: return
        if (callback.uuid != uuid) {
            return
        }
        callbacks.remove(player.uuid)
        callback.cancel(player)
    }

    fun handleCallback(player: ServerPlayer, uuid: UUID, index: Int) {
        val callback = callbacks[player.uuid] ?: return
        callbacks.remove(player.uuid)
        if (callback.uuid != uuid) {
            Cobblemon.LOGGER.warn("A move select callback ran but with a mismatching UUID from ${player.gameProfile.name}. Hacking attempts?")
        } else if (index >= callback.shownMoves.size) {
            Cobblemon.LOGGER.warn("${player.gameProfile.name} used move select callback with an index that was out of bounds. Hacking attempts? Tried $index, possible size was ${callback.shownMoves.size}")
        } else if (!callback.shownMoves[index].enabled) {
            Cobblemon.LOGGER.warn("${player.gameProfile.name} used move select callback with a move that is not enabled. Hacking attempts?")
        } else {
            callback.handler(player, index, callback.shownMoves[index])
        }
    }
}

class MoveSelectCallback(
    val uuid: UUID = UUID.randomUUID(),
    val shownMoves: List<MoveSelectDTO>,
    val cancel: (ServerPlayer) -> Unit = {},
    val handler: (ServerPlayer, Int, MoveSelectDTO) -> Unit
)

class MoveSelectDTO(val moveTemplate: MoveTemplate, var enabled: Boolean, val pp: Int = -1, val ppMax: Int = -1) {
    @JvmOverloads
    constructor(move: Move, enabled: Boolean = true): this(moveTemplate = move.template, enabled = enabled, pp = move.currentPp, ppMax = move.maxPp)
    @JvmOverloads
    constructor(move: InBattleMove, enabled: Boolean = true): this(moveTemplate = Moves.getByNameOrDummy(move.move), enabled = enabled, pp = move.pp, ppMax = move.maxpp)
    constructor(buffer: RegistryFriendlyByteBuf): this(
        moveTemplate = Moves.getByNameOrDummy(buffer.readString()),
        enabled = buffer.readBoolean(),
        pp = buffer.readSizedInt(IntSize.BYTE),
        ppMax = buffer.readSizedInt(IntSize.BYTE)
    )

    fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(moveTemplate.name)
        buffer.writeBoolean(enabled)
        buffer.writeSizedInt(IntSize.BYTE, pp)
        buffer.writeSizedInt(IntSize.BYTE, ppMax)
    }
}