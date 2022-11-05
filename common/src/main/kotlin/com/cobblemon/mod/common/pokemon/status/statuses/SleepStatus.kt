/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status.statuses

import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.cobblemonResource
class SleepStatus : PersistentStatus(
    name = cobblemonResource("sleep"),
    showdownName = "slp",
    applyMessage = "cobblemon.status.sleep.apply",
    removeMessage = "cobblemon.status.sleep.woke",
    defaultDuration = IntRange(180, 300)
)