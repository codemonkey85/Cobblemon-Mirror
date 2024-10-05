/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.settings

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.net.settings.ServerSettingsPacketHandler
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket

/**
 * A holder for config options the server wants to sync with the client.
 * See [ServerSettingsPacket] & [ServerSettingsPacketHandler] for more information.
 *
 * @author Licious
 * @since September 27th, 2022
 */
object ServerSettings {

    var preventCompletePartyDeposit = Cobblemon.config.preventCompletePartyDeposit
    var displayEntityLevelLabel = Cobblemon.config.displayEntityLevelLabel
    var displayEntityNameLabel = Cobblemon.config.displayEntityNameLabel
}