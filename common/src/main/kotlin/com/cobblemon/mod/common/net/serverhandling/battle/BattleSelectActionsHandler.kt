/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.battle

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.exception.IllegalActionChoiceException
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object BattleSelectActionsHandler : ServerNetworkPacketHandler<BattleSelectActionsPacket> {
    override fun handle(packet: BattleSelectActionsPacket, server: MinecraftServer, player: ServerPlayer) {
        val battle = BattleRegistry.getBattle(packet.battleId) ?: return
        val actor = battle.actors.find { player.uuid in it.getPlayerUUIDs() } ?: return
        if (!actor.mustChoose) {
            return
        }
        try {
            actor.setActionResponses(packet.showdownActionResponses)
        } catch (e: IllegalActionChoiceException) {
            player.sendSystemMessage(e.message!!.red())
            actor.sendUpdate(BattleQueueRequestPacket(actor.request!!))
            actor.sendUpdate(BattleMakeChoicePacket())
        }
    }
}