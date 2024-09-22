package com.cobblemon.mod.common.client.requests

import com.cobblemon.mod.common.api.interaction.PlayerActionRequest
import java.util.*

/**
 * Client representation of a received [PlayerActionRequest].
 *
 * @author Segfault Guy
 * @since September 21st, 2024
 */
interface ClientPlayerActionRequest {
    /** The unique ID of this request. */
    val requestID: UUID

    /** The amount of seconds this request is valid for. */
    val expiryTime: Int
}