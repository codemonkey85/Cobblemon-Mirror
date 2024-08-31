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
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to tell the player they can't do a thing.
 *
 * @author Yaseen
 * April 22nd, 2023
 */
class BattleMadeInvalidChoicePacket : NetworkPacket<BattleMadeInvalidChoicePacket> {
    companion object {
        val ID = cobblemonResource("battle_made_invalid_choice")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleMadeInvalidChoicePacket()
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
}