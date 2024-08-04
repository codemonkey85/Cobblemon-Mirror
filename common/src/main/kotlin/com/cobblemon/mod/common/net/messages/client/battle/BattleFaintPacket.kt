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
 * Reports that a specific Pokémon fainted. This triggers a specific tile animation.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleFaintHandler].
 *
 * @author Hiroku
 * @since May 22nd, 2022
 */
class BattleFaintPacket(val pnx: String) : NetworkPacket<BattleFaintPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(pnx)
    }
    companion object {
        val ID = cobblemonResource("battle_faint")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleFaintPacket(buffer.readString())
    }
}