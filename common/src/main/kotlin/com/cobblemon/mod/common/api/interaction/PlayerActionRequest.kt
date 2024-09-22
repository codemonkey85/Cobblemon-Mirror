package com.cobblemon.mod.common.api.interaction

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

    /** The unique ID of the player receiving this request. */
    val targetID: UUID

    /** The amount of seconds this request is valid for. */
    val expiryTime: Int
}