/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import net.minecraft.server.MinecraftServer
import java.util.UUID

/**
 * Loads and saves some kind of InstancedPlayerData
 */
abstract class PlayerDataStoreBackend<T : InstancedPlayerData>(val dataType: PlayerInstancedDataStoreType) {
    abstract fun load(uuid: UUID): T
    abstract fun save(playerData: T)
    open fun initialize(store: T) {}

    abstract fun setup(server: MinecraftServer)
}