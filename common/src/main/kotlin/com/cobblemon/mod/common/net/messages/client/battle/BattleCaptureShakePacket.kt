/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when a PokéBall capturing a battle Pokémon just shook. The direction is included.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleCaptureShakeHandler].
 *
 * @author Hiroku
 * @since July 3rd, 2022
 */
class BattleCaptureShakePacket(val targetPNX: String, val direction: Boolean) : NetworkPacket<BattleCaptureShakePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(targetPNX)
        buffer.writeBoolean(direction)
    }
    companion object {
        val ID = cobblemonResource("battle_capture_shake")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleCaptureShakePacket(buffer.readString(), buffer.readBoolean())
    }
}