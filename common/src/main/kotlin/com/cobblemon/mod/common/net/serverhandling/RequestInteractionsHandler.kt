/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.server.RequestPlayerInteractionsPacket
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext
import java.util.EnumSet
import kotlin.math.pow

object RequestInteractionsHandler : ServerNetworkPacketHandler<RequestPlayerInteractionsPacket> {

    override fun handle(
        packet: RequestPlayerInteractionsPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        val world = player.level()
        val targetPlayerEntity = world.getPlayerByUUID(packet.targetId)
        val options = EnumSet.noneOf(PlayerInteractOptionsPacket.Options::class.java)
        if (targetPlayerEntity != null && player.traceFirstEntityCollision(
            entityClass = LivingEntity::class.java,
            ignoreEntity = player,
            maxDistance = Cobblemon.config.battleSpectateMaxDistance,
            collideBlock = ClipContext.Fluid.NONE
        ) == targetPlayerEntity) {
            //We could potentially check if the targeted player has pokemon here
            val squaredDistance = targetPlayerEntity.position().distanceToSqr(player.position())
            if (squaredDistance <= Cobblemon.config.tradeMaxDistance.pow(2)) {
                options.add(PlayerInteractOptionsPacket.Options.TRADE)
            }

            val isTargetBattling = BattleRegistry.getBattleByParticipatingPlayerId(packet.targetId) != null
            if (isTargetBattling and Cobblemon.config.allowSpectating && squaredDistance <= Cobblemon.config.battleSpectateMaxDistance.pow(2)) {
                options.add(PlayerInteractOptionsPacket.Options.SPECTATE_BATTLE)
            }
            else if (squaredDistance <= Cobblemon.config.BattlePvPMaxDistance.pow(2)) {
                // LOS and distance checks passed, now check parties and add appropriate options
                val playerPartyCount = player.party().count { pokemon -> !pokemon.isFainted() }
                val targetPartyCount = (targetPlayerEntity as ServerPlayer).party().count { pokemon -> !pokemon.isFainted() }
                if (playerPartyCount >= 1 && targetPartyCount >= 1) {
                    options.add(PlayerInteractOptionsPacket.Options.SINGLE_BATTLE)
                    //options.add(PlayerInteractOptionsPacket.Options.ROYAL_BATTLE)
                    if (BattleRegistry.playerToTeam[player.uuid] != null && BattleRegistry.playerToTeam[packet.targetId] !== null) {
                        if (BattleRegistry.playerToTeam[player.uuid]?.teamID != BattleRegistry.playerToTeam[packet.targetId]?.teamID) {
                            options.add(PlayerInteractOptionsPacket.Options.MULTI_BATTLE)
                        } else {
                            options.add(PlayerInteractOptionsPacket.Options.TEAM_LEAVE)
                        }
                    } else if (BattleRegistry.playerToTeam[player.uuid] === null && BattleRegistry.playerToTeam[packet.targetId] === null) {
                        // TODO: Max team size checking, allow for team of size > 2
                        options.add(PlayerInteractOptionsPacket.Options.TEAM_REQUEST)
                    }
                    if (playerPartyCount >= 2 && targetPartyCount >= 2) {
                        options.add(PlayerInteractOptionsPacket.Options.DOUBLE_BATTLE)
                        if (playerPartyCount >= 3 && targetPartyCount >= 3) {
                            options.add(PlayerInteractOptionsPacket.Options.TRIPLE_BATTLE)
                        }
                    }

                }
            }
        }
        if (!options.isEmpty()) {
            PlayerInteractOptionsPacket(options, packet.targetId, packet.targetNumericId, packet.pokemonId).sendToPlayer(player)
        }
    }

}