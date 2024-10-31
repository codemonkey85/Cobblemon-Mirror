/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket
import com.cobblemon.mod.common.net.messages.server.battle.SpectateBattlePacket
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext
import org.apache.logging.log4j.LogManager

object SpectateBattleHandler : ServerNetworkPacketHandler<SpectateBattlePacket> {
    val LOGGER = LogManager.getLogger()
    override fun handle(
        packet: SpectateBattlePacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        val battle = BattleRegistry.getBattleByParticipatingPlayerId(packet.targetedEntityId)
        if (battle != null && Cobblemon.config.allowSpectating) {
            val target = battle.actors.filterIsInstance<PlayerBattleActor>().firstOrNull { it.uuid == packet.targetedEntityId }

            // Check los and range
            val targetedPlayerEntity = packet.targetedEntityId.getPlayer() ?: return
            if (player.traceFirstEntityCollision(
                            entityClass = LivingEntity::class.java,
                            ignoreEntity = player,
                            maxDistance = Cobblemon.config.battleSpectateMaxDistance,
                            collideBlock = ClipContext.Fluid.NONE) != targetedPlayerEntity) {
                player.sendSystemMessage(lang("ui.interact.failed").yellow())
                return
            }
            battle.spectators.add(player.uuid)
            player.sendPacket(BattleInitializePacket(battle, null))
            player.sendPacket(BattleMessagePacket(battle.chatLog))
            target?.battleTheme?.let { player.sendPacket(BattleMusicPacket(it)) }
        }
        else {
            LOGGER.error("Battle of player id ${packet.targetedEntityId} not found (${player.uuid} tried spectating)")
        }
    }

}