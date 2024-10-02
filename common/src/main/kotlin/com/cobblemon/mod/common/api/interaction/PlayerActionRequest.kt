/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.interaction

import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Represents an interaction request between players.
 *
 * @author Segfault Guy
 * @since September 3rd, 2024
 */
interface PlayerActionRequest {
    /** The unique ID of this request. */
    val requestID: UUID

    /** The amount of seconds this request is valid for. */
    val expiryTime: Int
}

/**
 * An outbound [PlayerActionRequest].
 *
 * @author Segfault Guy
 * @since September 29th, 2024
 */
interface ServerPlayerActionRequest : PlayerActionRequest {
    /** The unique ID of the player receiving this request. */
    val targetID: UUID

    companion object {
        /** System message to inform [player] about [langKey] request. */
        fun notify(langKey: String, player: ServerPlayer, relatedName: Component? = null, vararg params: Any) {
            val lang = relatedName?.let { lang(langKey, relatedName.copy().aqua(), *params) } ?: lang(langKey, *params)
            player.sendSystemMessage(lang, false)
        }
    }
}