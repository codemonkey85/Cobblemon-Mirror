/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.battles.BattleFormat
import java.util.UUID

class ClientBattleChallenge(
    val challengeId: UUID,
    val challengerIds: List<UUID>,
    val battleFormat: BattleFormat? = null
)