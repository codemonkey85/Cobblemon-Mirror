/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.net.serverhandling.ChallengeResponseHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet sent when a player responds to a [BattleChallenge] after receiving the respective [BattleChallengeNotificationPacket].
 *
 * Handled by [ChallengeResponseHandler].
 *
 * @param requestID The unique identifier of the request that the player is responding to.
 * @param accept Whether the player accepted the team request.
 *
 * @author JazzMcNade
 * @since March 12th, 2024
 */
class BattleChallengeResponsePacket(val targetedEntityId: Int, val requestID: UUID, val selectedPokemonId: UUID, val accept: Boolean) : NetworkPacket<BattleChallengeResponsePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(this.targetedEntityId)
        buffer.writeUUID(this.requestID)
        buffer.writeUUID(this.selectedPokemonId)
        buffer.writeBoolean(accept)
    }
    companion object {
        val ID = cobblemonResource("battle_challenge_response")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleChallengeResponsePacket(buffer.readInt(), buffer.readUUID(), buffer.readUUID(), buffer.readBoolean())
    }
}