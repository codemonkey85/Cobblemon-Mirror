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
import java.util.EnumMap

/**
 * Used to populate the player interaction menu
 *
 *
 * @author Apion
 * @since November 5th, 2023
 */
class PlayerInteractOptionsPacket(
    val options: EnumMap<Options, OptionStatus>,
    val targetId: UUID,
    val numericTargetId: Int,
    val selectedPokemonId: UUID,
    ) : NetworkPacket<PlayerInteractOptionsPacket> {
    companion object {
        val ID = cobblemonResource("player_interactions")
        fun decode(buffer: RegistryFriendlyByteBuf) = PlayerInteractOptionsPacket(
            readOptionsMap(buffer),
            buffer.readUUID(),
            buffer.readInt(),
            buffer.readUUID()
        )

        private fun readOptionsMap(buffer: RegistryFriendlyByteBuf) : EnumMap<Options, OptionStatus> {
            val size = buffer.readInt()
            val options : EnumMap<Options, OptionStatus> = EnumMap<Options, OptionStatus>(Options::class.java)
            repeat(size) {
                val key = buffer.readEnum(Options::class.java)
                val value = buffer.readEnum(OptionStatus::class.java)
                options[key] = value
            }
            return options
        }
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(options.size)
        for ((key, value) in options) {
            buffer.writeEnum(key)
            buffer.writeEnum(value)
        }
        buffer.writeUUID(targetId)
        buffer.writeInt(numericTargetId)
        buffer.writeUUID(selectedPokemonId)
    }

    enum class Options {
        SINGLE_BATTLE,
        DOUBLE_BATTLE,
        TRIPLE_BATTLE,
        MULTI_BATTLE,
        ROYAL_BATTLE,
        SPECTATE_BATTLE,
        TRADE,
        TEAM_REQUEST,
        TEAM_LEAVE,
    }

    enum class OptionStatus {
        AVAILABLE,
        TOO_FAR,
        INSUFFICIENT_POKEMON,
        OTHER
    }

}