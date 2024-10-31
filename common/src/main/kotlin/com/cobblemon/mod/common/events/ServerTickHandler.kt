/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer

object ServerTickHandler {
    private var secondsTick = 0

    fun onTick(server: MinecraftServer) {
        Cobblemon.bestSpawner.spawnerManagers.forEach { it.onServerTick() }
        BattleRegistry.tick()

        secondsTick++

        if (secondsTick == 20) {
            secondsTick = 0

            // Party tick
            for (player in server.playerList.players) {
                player.party().onSecondPassed(player)
            }
        }
    }
}