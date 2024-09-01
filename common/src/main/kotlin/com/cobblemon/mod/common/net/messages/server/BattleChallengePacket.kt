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
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

class BattleChallengePacket(val targetedEntityId: Int, val selectedPokemonId: UUID) : NetworkPacket<BattleChallengePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(this.targetedEntityId)
        buffer.writeUUID(this.selectedPokemonId)
    }
    companion object {
        val ID = cobblemonResource("battle_challenge")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleChallengePacket(buffer.readInt(), buffer.readUUID())
    }
}