/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.MinecraftServer
import java.util.UUID
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ClipContext

object ChallengeHandler : ServerNetworkPacketHandler<BattleChallengePacket> {
    override fun handle(packet: BattleChallengePacket, server: MinecraftServer, player: ServerPlayer) {
        if(player.isSpectator) return

        val targetedEntity = player.level().getEntity(packet.targetedEntityId)?.let {
            if (it is PokemonEntity) {
                val owner = it.owner
                if (owner != null) {
                    return@let owner
                }
            }
            return@let it
        } ?: return

        // Check los and range
        val maxDistance = if(targetedEntity is PokemonEntity) Cobblemon.config.battleWildMaxDistance else Cobblemon.config.BattlePvPMaxDistance
        if (player.traceFirstEntityCollision(
                entityClass = LivingEntity::class.java,
                ignoreEntity = player,
                maxDistance = maxDistance,
                collideBlock = ClipContext.Fluid.NONE) != targetedEntity) {
            if(targetedEntity !is PokemonEntity) {
                player.sendSystemMessage(lang("cobblemon.ui.interact.failed").yellow())
            }
            return
        }

        val leadingPokemon = player.party()[packet.selectedPokemonId]?.uuid ?: return


        when (targetedEntity) {
            is PokemonEntity -> {
                if (!targetedEntity.canBattle(player)) {
                    return
                }
                /*
                if (targetedEntity.isOwner(player))
                    return@runOnServer
                 */
                BattleBuilder.pve(player, targetedEntity, leadingPokemon).ifErrored { it.sendTo(player) { it.red() } }
            }
            is ServerPlayer -> {
                // Bandaid for odd desync thing with data tracker
                if (player == targetedEntity) {
                    return
                }
                val existingChallenge = BattleRegistry.pvpChallenges[player.uuid]
                if (existingChallenge != null && !existingChallenge.isExpired() && existingChallenge.challengedPlayerUUID == targetedEntity.uuid) {
                    // Overwrite the challenge or do nothing.
                    // send a message about there being an existing challenge
                    player.sendSystemMessage(lang("challenge.pending", targetedEntity.name).yellow())
                } else {
                    if (packet.battleFormat.battleType.name == BattleTypes.MULTI.name) {
                        // check for team
                        val existingPlayerTeam = BattleRegistry.playerToTeam[player.uuid]
                        val existingTargetTeam = BattleRegistry.playerToTeam[targetedEntity.uuid]
                        if(existingPlayerTeam != null && existingTargetTeam != null && existingTargetTeam.teamID != existingPlayerTeam.teamID) {
                            // Send a request to start a battle
                            val challenge = BattleRegistry.BattleChallenge(UUID.randomUUID(), existingTargetTeam.teamID, leadingPokemon, packet.battleFormat)
                            BattleRegistry.pvpChallenges[existingPlayerTeam.teamID] = challenge
                            afterOnServer(seconds = challenge.expiryTimeSeconds.toFloat()) {
                                BattleRegistry.removeChallenge(existingPlayerTeam.teamID, challengeId = challenge.challengeId)
                            }
                            // Notify everyone of the challenge

                            // Notify challenging team
                            existingPlayerTeam.teamPlayersUUID.forEach { uuid ->
                                val serverPlayerEntity = uuid.getPlayer()
                                serverPlayerEntity?.sendSystemMessage(lang("challenge.multi.sender", targetedEntity.name, existingTargetTeam.teamPlayersUUID.size))
                            }
                            // Notify challenged tam
                            CobblemonNetwork.sendPacketToPlayers(
                                existingTargetTeam.teamPlayersUUID.map { it.getPlayer() }.mapNotNull { it },
                                BattleChallengeNotificationPacket(challenge.challengeId, existingPlayerTeam.teamPlayersUUID, existingPlayerTeam.teamPlayersUUID.mapNotNull { it.getPlayer()?.name?.copy()?.aqua() }, BattleFormat.GEN_9_MULTI)
                            )
                        }
                    } else {
                        val challenge = BattleRegistry.BattleChallenge(UUID.randomUUID(), targetedEntity.uuid, leadingPokemon, packet.battleFormat)
                        BattleRegistry.pvpChallenges[player.uuid] = challenge
                        afterOnServer(seconds = challenge.expiryTimeSeconds.toFloat()) {
                            BattleRegistry.removeChallenge(player.uuid, challengeId = challenge.challengeId)
                        }

                        val battleFormatLang = when (packet.battleFormat.battleType.name) {
                            BattleTypes.DOUBLES.name -> "battle.types.doubles"
                            BattleTypes.TRIPLES.name -> "battle.types.triples"
                            BattleTypes.MULTI.name -> "battle.types.multi"
                            BattleTypes.ROYAL.name -> "battle.types.freeforall"
                            else -> "battle.types.singles"
                        }

                        targetedEntity.sendPacket(BattleChallengeNotificationPacket(challenge.challengeId, player.uuid, player.name.copy().aqua(), packet.battleFormat))
                        player.sendSystemMessage(lang("challenge.sender", targetedEntity.name, lang(battleFormatLang)).yellow())
                    }
                }
            }
            else -> {
                // Unrecognized challenge target. NPCs will probably go here.
            }
        }
    }
}