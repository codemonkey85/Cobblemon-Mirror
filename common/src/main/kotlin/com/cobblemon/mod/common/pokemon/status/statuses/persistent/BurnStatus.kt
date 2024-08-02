/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status.statuses.persistent

import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.cobblemonResource
class BurnStatus : PersistentStatus(
    name = cobblemonResource("burn"),
    showdownName = "brn",
    applyMessage = "cobblemon.status.burn.apply",
    removeMessage = "cobblemon.status.burn.cure",
    defaultDuration = IntRange(180, 300)
)