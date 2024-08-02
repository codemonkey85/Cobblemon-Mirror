/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.cobblemon.mod.common.api.storage.player.factory.JsonPlayerDataStoreFactory
import com.cobblemon.mod.common.api.storage.player.factory.PlayerDataStoreFactory
import net.minecraft.world.entity.player.Player
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

class PlayerDataStoreManager {


    private var factory: PlayerDataStoreFactory = JsonPlayerDataStoreFactory()
    fun setFactory(factory: PlayerDataStoreFactory) {
        this.factory = factory;
    }

    private fun registerSaveScheduler() = ScheduledTask.Builder()
        .execute { saveAll() }
        .delay(30f)
        .interval(120f)
        .infiniteIterations()
        .tracker(ServerTaskTracker)
        .build()

    fun setup(server: MinecraftServer) {
        registerSaveScheduler()
        (factory as? JsonPlayerDataStoreFactory)?.setup(server)
    }

    fun get(player: Player): PlayerData = factory.load(player.uuid);

    fun saveAll() = factory.saveAll();

    fun saveSingle(playerData: PlayerData) = factory.save(playerData)

    fun onPlayerDisconnect(player: ServerPlayer) {
        factory.onPlayerDisconnect(player.uuid);
    }
}