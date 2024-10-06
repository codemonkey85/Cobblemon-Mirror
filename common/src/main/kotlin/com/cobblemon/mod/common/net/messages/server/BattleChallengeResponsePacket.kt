/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class BattleChallengeResponsePacket(val targetedEntityId: Int, val selectedPokemonId: UUID, val accept: Boolean) : NetworkPacket<BattleChallengeResponsePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(this.targetedEntityId)
        buffer.writeUUID(this.selectedPokemonId)
        buffer.writeBoolean(accept)
    }
    companion object {
        val ID = cobblemonResource("battle_challenge_response")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleChallengeResponsePacket(buffer.readInt(), buffer.readUUID(), buffer.readBoolean())
    }
}