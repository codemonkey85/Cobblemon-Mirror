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
 * Tells the participants that the capture on the specified Pokémon has finished.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleCaptureEndHandler].
 *
 * @author Hiroku
 * @since July 2nd, 2022
 */
class BattleCaptureEndPacket(val targetPNX: String, val succeeded: Boolean) : NetworkPacket<BattleCaptureEndPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(targetPNX)
        buffer.writeBoolean(succeeded)
    }
    companion object {
        val ID = cobblemonResource("battle_capture_end")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleCaptureEndPacket(buffer.readString(), buffer.readBoolean())
    }
}