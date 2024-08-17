/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.trackeddata

/**
 * The types of events that can trigger updates in tracked data
 *
 * @author Apion
 * @since February 24, 2024
 */
enum class EventTriggerType {
    CAUGHT,
    EVOLVE,
    TRADE,
    BATTLE_START,
    SEEN,
    SCANNED
}