/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import java.util.UUID
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

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
                val existingChallenge = BattleRegistry.pvpChallenges[targetedEntity.uuid]
                var existingChallengePokemon = existingChallenge?.selectedPokemonId
                if (existingChallenge != null && !existingChallenge.isExpired() && existingChallenge.challengedPlayerUUID == player.uuid) {
                    if (targetedEntity.party()[existingChallengePokemon!!] == null) {
                        if (targetedEntity.party().none()) {
                            player.sendSystemMessage(battleLang("error.no_pokemon_opponent"))
                            targetedEntity.sendSystemMessage(battleLang("error.no_pokemon"))
                            BattleRegistry.removeChallenge(targetedEntity.uuid)
                            return
                        }
                        existingChallengePokemon = targetedEntity.party().first().uuid
                    }
                    BattleBuilder.pvp1v1(player, targetedEntity, leadingPokemon, existingChallengePokemon)
                    BattleRegistry.removeChallenge(targetedEntity.uuid)
                } else {
                    val challenge = BattleRegistry.BattleChallenge(UUID.randomUUID(), targetedEntity.uuid, leadingPokemon)
                    BattleRegistry.pvpChallenges[player.uuid] = challenge
                    afterOnServer(seconds = challenge.expiryTimeSeconds.toFloat()) {
                        BattleRegistry.removeChallenge(player.uuid, challengeId = challenge.challengeId)
                    }
                    targetedEntity.sendPacket(BattleChallengeNotificationPacket(challenge.challengeId, player.uuid, player.name.copy().aqua()))
                    player.sendSystemMessage(lang("challenge.sender", targetedEntity.name).yellow())
                }
            }
            else -> {
                // Unrecognized challenge target. NPCs will probably go here.
            }
        }
    }
}