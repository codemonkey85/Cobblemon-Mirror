/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.UUID

/**
 * A factory that produces and saves some type of [InstancedPlayerData]
 *
 * @author Apion
 * @since February 21, 2024
 */
interface PlayerInstancedDataFactory<T : InstancedPlayerData> {
    fun setup(server: MinecraftServer)

    fun getForPlayer(player: Player): T {
        return getForPlayer(player.uuid)
    }

    fun getForPlayer(playerId: UUID) : T

    fun saveAll()

    fun saveSingle(player: Player) {
        saveSingle(player.uuid)
    }

    fun saveSingle(playerId: UUID)

    fun onPlayerDisconnect(player: ServerPlayer)

    fun sendToPlayer(player: ServerPlayer)
}