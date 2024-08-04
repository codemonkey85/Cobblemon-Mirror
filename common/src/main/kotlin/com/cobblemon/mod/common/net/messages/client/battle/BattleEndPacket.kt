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
 * Tells the client to terminate its battle reference
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleEndHandler].
 *
 * @author Hiroku
 * @since May 6th, 2022
 */
class BattleEndPacket : NetworkPacket<BattleEndPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
    companion object {
        val ID = cobblemonResource("battle_end")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleEndPacket()
    }
}