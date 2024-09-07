/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.EnumSet
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Used to populate the player interaction menu
 *
 *
 * @author Apion
 * @since November 5th, 2023
 */
class PlayerInteractOptionsPacket(
    val options: EnumSet<Options>,
    val targetId: UUID,
    val numericTargetId: Int,
    val selectedPokemonId: UUID
    ) : NetworkPacket<PlayerInteractOptionsPacket> {
    companion object {
        val ID = cobblemonResource("player_interactions")
        fun decode(buffer: RegistryFriendlyByteBuf) = PlayerInteractOptionsPacket(
            buffer.readEnumSet(Options::class.java),
            buffer.readUUID(),
            buffer.readInt(),
            buffer.readUUID()
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeEnumSet(options, Options::class.java)
        buffer.writeUUID(targetId)
        buffer.writeInt(numericTargetId)
        buffer.writeUUID(selectedPokemonId)
    }

    enum class Options {
        BATTLE,
        SPECTATE_BATTLE,
        TRADE
    }

}