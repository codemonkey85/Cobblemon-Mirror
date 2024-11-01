/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.requests

import com.cobblemon.mod.common.api.interaction.PlayerActionRequest
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.util.UUID

/**
 * An inbound [PlayerActionRequest].
 *
 * @author Segfault Guy
 * @since September 21st, 2024
 */
abstract class ClientPlayerActionRequest(expiryTime: Int) : ClientPlayerIcon(expiryTime), PlayerActionRequest {

    companion object {
        /** Client message to inform the player about a [langKey] request from [senderID]. */
        fun notify(langKey: String, senderID: UUID, vararg params: Any) {
            val sender = Minecraft.getInstance().level?.players()?.find { it.uuid == senderID }
            val senderName = sender?.name?.copy()?.aqua() ?: Component.literal("NULL").red()
            val lang = lang(langKey, senderName, *params).yellow()
            Minecraft.getInstance().player!!.displayClientMessage(lang, false)
        }
    }
}