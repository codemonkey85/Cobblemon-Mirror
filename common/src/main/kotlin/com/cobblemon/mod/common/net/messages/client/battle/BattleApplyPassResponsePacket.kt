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
 * Tells a specific player that they should choose a battle capture response for the next Pokémon request in their queue.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleApplyPassResponseHandler].
 *
 * @author Hiroku
 * @since July 3rd, 2022
 */
class BattleApplyPassResponsePacket : NetworkPacket<BattleApplyPassResponsePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
    companion object {
        val ID = cobblemonResource("battle_apply_pass_response")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleApplyPassResponsePacket()
    }
}