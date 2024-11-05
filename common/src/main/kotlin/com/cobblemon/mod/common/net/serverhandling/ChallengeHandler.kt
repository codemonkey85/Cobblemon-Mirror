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
import com.cobblemon.mod.common.battles.BattleTypes
import com.cobblemon.mod.common.battles.ChallengeManager
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Processes a player's interaction request to battle with another player. If valid, creates a respective [BattleChallenge]
 * and sends a [BattleChallengeNotificationPacket] to the player to decide upon.
 *
 * @author Hiroku
 * @since April 23rd, 2022
 */
object ChallengeHandler : ServerNetworkPacketHandler<BattleChallengePacket> {
    override fun handle(packet: BattleChallengePacket, server: MinecraftServer, player: ServerPlayer) {
        val targetedEntity = player.level().getEntity(packet.targetedEntityId)?.let {
            when (it) {
                is PokemonEntity -> it.owner
                is ServerPlayer -> it
                else -> null
            }
        } ?: return

        ChallengeManager.setLead(player, packet.selectedPokemonId)
        if (targetedEntity !is ServerPlayer) return
        val challenge =
            if (packet.battleFormat.battleType.name == BattleTypes.MULTI.name)
                ChallengeManager.MultiBattleChallenge(player, targetedEntity, packet.selectedPokemonId, packet.battleFormat)
            else
                ChallengeManager.SinglesBattleChallenge(player, targetedEntity, packet.selectedPokemonId, packet.battleFormat)
        ChallengeManager.sendRequest(challenge)
    }
}