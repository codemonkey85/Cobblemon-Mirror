/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.settings

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import net.minecraft.client.Minecraft

object ServerSettingsPacketHandler : ClientNetworkPacketHandler<ServerSettingsPacket> {
    override fun handle(packet: ServerSettingsPacket, client: Minecraft) {
        ServerSettings.preventCompletePartyDeposit = packet.preventCompletePartyDeposit
        ServerSettings.displayEntityLevelLabel = packet.displayEntityLevelLabel
    }

}