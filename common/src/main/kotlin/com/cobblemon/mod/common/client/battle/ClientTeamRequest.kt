package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.client.requests.ClientPlayerActionRequest
import java.util.*

data class ClientTeamRequest(
    override val requestID: UUID,
    override val expiryTime: Int
) : ClientPlayerActionRequest, ClientPlayerIcon(expiryTime)