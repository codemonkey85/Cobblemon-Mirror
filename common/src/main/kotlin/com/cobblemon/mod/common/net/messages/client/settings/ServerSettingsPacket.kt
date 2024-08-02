/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.settings

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A packet that will sync simple config settings to the client that shouldn't require to be data pack powered.
 *
 * @author Licious
 * @since September 25th, 2022
 */
class ServerSettingsPacket internal constructor(val preventCompletePartyDeposit: Boolean, val displayEntityLevelLabel: Boolean) : NetworkPacket<ServerSettingsPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(Cobblemon.config.preventCompletePartyDeposit)
        buffer.writeBoolean(Cobblemon.config.displayEntityLevelLabel)
    }
    companion object {
        val ID = cobblemonResource("server_settings")
        fun decode(buffer: RegistryFriendlyByteBuf) = ServerSettingsPacket(buffer.readBoolean(), buffer.readBoolean())
    }
}