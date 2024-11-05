/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.net.serverhandling.ChallengeHandler
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet fired when a player makes an interaction request to challenge another player to a Pokemon battle.
 *
 * Handled by [ChallengeHandler].
 *
 * @param targetedEntityId The ID of the player who's the target of this interaction request.
 * @param battleFormat The showdown format of the challenge.
 *
 * @author JazzMcNade
 * @since April 15th, 2024
 */
class BattleChallengePacket(val targetedEntityId: Int, val selectedPokemonId: UUID, val battleFormat: BattleFormat) : NetworkPacket<BattleChallengePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(this.targetedEntityId)
        buffer.writeUUID(this.selectedPokemonId)
        battleFormat.saveToBuffer(buffer)
    }
    companion object {
        val ID = cobblemonResource("battle_challenge")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleChallengePacket(buffer.readInt(), buffer.readUUID(), BattleFormat.loadFromBuffer(buffer))
    }
}