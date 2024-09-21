/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents.BATTLE_VICTORY
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor

object BattleHandler : EventHandler {
    override fun registerListeners() {
        BATTLE_VICTORY.subscribe(Priority.NORMAL, ::onBattleEnd)
    }

    fun onBattleEnd(event: BattleVictoryEvent) {
        var players = event.winners.filter { it is PlayerBattleActor }
        players += event.losers.filter { it is PlayerBattleActor }
        players.forEach {
            val playerActor = it as PlayerBattleActor
            val playerData = Cobblemon.playerDataManager.getGenericData(playerActor.uuid)
            playerData.battlesFought++
            playerActor.entity ?: return@forEach
            playerData.sendToPlayer(playerActor.entity!!)
        }
    }
}