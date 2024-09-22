/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.client.requests.ClientPlayerActionRequest
import java.util.*

data class ClientBattleChallenge(
    override val requestID: UUID,
    override val expiryTime: Int,
    val battleFormat: BattleFormat
) : ClientPlayerActionRequest, ClientPlayerIcon(expiryTime)

data class ClientTeamRequest(
    override val requestID: UUID,
    override val expiryTime: Int
) : ClientPlayerActionRequest, ClientPlayerIcon(expiryTime)