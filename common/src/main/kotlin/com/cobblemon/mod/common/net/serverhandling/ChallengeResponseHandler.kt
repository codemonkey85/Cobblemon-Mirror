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
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.*
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.BattleChallengeResponsePacket
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.Vec3

object ChallengeResponseHandler : ServerNetworkPacketHandler<BattleChallengeResponsePacket> {
    override fun handle(packet: BattleChallengeResponsePacket, server: MinecraftServer, player: ServerPlayer) {
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
        if (player.traceFirstEntityCollision(
                        entityClass = LivingEntity::class.java,
                        ignoreEntity = player,
                        maxDistance = Cobblemon.config.BattlePvPMaxDistance,
                        collideBlock = ClipContext.Fluid.NONE) != targetedEntity) {
            player.sendSystemMessage(lang("ui.interact.failed").yellow())
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
                // Check in on battle requests, if the other player has challenged me, this starts the battle
                val existingChallenge = BattleRegistry.pvpChallenges[targetedEntity.uuid] ?: BattleRegistry.pvpChallenges[BattleRegistry.playerToTeam[targetedEntity.uuid]?.teamID]
                var existingChallengePokemon = existingChallenge?.selectedPokemonId
                if (existingChallenge != null && !existingChallenge.isExpired() && (existingChallenge.challengedPlayerUUID == player.uuid || existingChallenge.challengedPlayerUUID == BattleRegistry.playerToTeam[player.uuid]?.teamID)) {

                    val battleFormat = existingChallenge.battleFormat

                    if(packet.accept) {
                        if(battleFormat.battleType.name == BattleTypes.MULTI.name) {
                            // Start a multibattle
                            val team1 = BattleRegistry.playerToTeam[targetedEntity.uuid]?.teamPlayersUUID
                            val team2 = BattleRegistry.playerToTeam[player.uuid]?.teamPlayersUUID
                            val targetTeamUUID = BattleRegistry.playerToTeam[targetedEntity.uuid]?.teamID
                            if(team1 == null || team2 == null || targetTeamUUID == null) {
                                // Error message
                                return
                            }

                            val players = (team1+team2).map { uuid -> uuid.getPlayer() }.mapNotNull { it }

                            if(players.count() != BattleRegistry.MAX_TEAM_MEMBER_COUNT * 2) {
                                // TODO: handle larger teams
                                BattleRegistry.removeChallenge(targetTeamUUID, existingChallenge.challengeId, true)
                                // Send error message that we don't have the correct number of players
                                players.forEach {
                                    it.sendSystemMessage(battleLang( "error.incorrect_player_count", players.count(), BattleRegistry.MAX_TEAM_MEMBER_COUNT).red())
                                }
                                return
                            }

                            val leadPokemon = players.mapNotNull { serverPlayerEntity ->
                                serverPlayerEntity.party().first().uuid
                            }

                            if(leadPokemon.count() != players.count()) {
                                // TODO: Error Message, missing team leads
                                BattleRegistry.removeChallenge(targetTeamUUID, existingChallenge.challengeId, true)
                                return
                            }

                            // Check if all players are in the same minecraft dimension
                            val dimension = player.level().dimension()
                            val playerInWrongDimension = players.firstOrNull { it.level().dimension() != dimension }
                            if(playerInWrongDimension != null) {
                                // TODO: Error message, not all players in the same Minecraft dimension
                                players.forEach {
                                    it.sendSystemMessage(battleLang("error.player_different_dimension"))
                                }
                                BattleRegistry.removeChallenge(targetTeamUUID, existingChallenge.challengeId, true)
                                return
                            }

                            // Check if all players all nearby
                            var averagePos = Vec3(0.0, 0.0, 0.0)
                            players.forEach {
                                averagePos = averagePos.add(it.position().multiply(1.0 / players.count(), 0.0, 1.0 / players.count()))
                            }
                            val farAwayPlayer = players.firstOrNull { it.position().subtract(0.0, it.position().y, 0.0).distanceToSqr(averagePos) > BattleRegistry.MAX_BATTLE_RADIUS * BattleRegistry.MAX_BATTLE_RADIUS }
                            if(farAwayPlayer != null) {
                                // Error message, player too far away
                                players.forEach {
                                    val langKey = if(it.uuid == farAwayPlayer.uuid) "error.player_distance.personal" else "error.player_distance"
                                    it.sendSystemMessage(battleLang(langKey, farAwayPlayer.name))
                                }
                                BattleRegistry.removeChallenge(targetTeamUUID, existingChallenge.challengeId, true)
                                return
                            }

                            BattleBuilder.pvp2v2(players, leadPokemon, battleFormat)
                            BattleRegistry.removeChallenge(targetTeamUUID, existingChallenge.challengeId, true)
                        } else {
                            if (targetedEntity.party()[existingChallengePokemon!!] == null) {
                                if (targetedEntity.party().none()) {
                                    player.sendSystemMessage(battleLang("error.no_pokemon_opponent"))
                                    targetedEntity.sendSystemMessage(battleLang("error.no_pokemon"))
                                    BattleRegistry.removeChallenge(targetedEntity.uuid)
                                    return
                                }
                                existingChallengePokemon = targetedEntity.party().first().uuid
                            }
                            BattleBuilder.pvp1v1(targetedEntity, player, existingChallengePokemon, leadingPokemon, battleFormat).ifErrored { it.sendTo(player) { it.red() }; it.sendTo(targetedEntity) { it.red() } }
                            BattleRegistry.removeChallenge(targetedEntity.uuid, existingChallenge.challengeId)
                        }

                    } else {
                        // Play messages to both sides that the battle was declined
                        // TODO: Will need a more generic condition when more multibattle rulesets are available
                        if (battleFormat.battleType.name == BattleTypes.MULTI.name) {
                            val targetedTeam = BattleRegistry.playerToTeam[targetedEntity.uuid]
                            if (targetedTeam != null) {
                                // Inform everyone involved that the battle was declined
                                val playerTeam = BattleRegistry.playerToTeam[player.uuid]
                                BattleRegistry.removeChallenge(targetedTeam.teamID, existingChallenge.challengeId, true)

                                // Send messages to player team
                                playerTeam?.teamPlayersUUID?.forEach { uuid ->
                                    if(uuid == player.uuid) {
                                        uuid.getPlayer()?.sendSystemMessage(lang("challenge.multibattle.decline.sender.personal", targetedEntity.name).yellow())
                                    } else {
                                        uuid.getPlayer()?.sendSystemMessage(lang("challenge.multibattle.decline.sender", player.name, targetedEntity.name).yellow())
                                    }
                                }

                                // Send messages to targeted team
                                targetedTeam.teamPlayersUUID.forEach { uuid ->
                                    uuid.getPlayer()?.sendSystemMessage(lang("challenge.multibattle.decline.receiver", player.name).yellow())
                                }
                            }
                        } else {
                            targetedEntity.sendSystemMessage(lang("challenge.decline.receiver", player.name).yellow())
                            player.sendSystemMessage(lang("challenge.decline.sender", targetedEntity.name).yellow())
                            BattleRegistry.removeChallenge(targetedEntity.uuid, existingChallenge.challengeId)
                        }

                    }
                }
            }
            else -> {
                // Unrecognized challenge target. NPCs will probably go here.
            }
        }
    }

}