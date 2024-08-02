/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.TimeRange.Companion.timeRanges
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition

/**
 * A range of time ticks for a world mainly to be used in [SpawningCondition]s. A time range
 * is defined as any number of tick ranges. For example, you can define a time range
 * as being between 0 and 12000 ticks or 13000 and 14000 ticks just for a single time
 * range. This is to deal with the limitation that Minecraft days go from 0 ticks to
 * 24000 ticks before looping back to 0.
 *
 * There are a series of in-built time ranges that you can replace if they don't suit
 * your definitions.
 *
 * If you want a time range to be referenced by name in [SpawningCondition]s, you need
 * to register it, by name, in [timeRanges].
 *
 * @author Hiroku
 * @since January 26th, 2022
 */
class TimeRange : IntRanges {
    companion object {
        val timeRanges = mutableMapOf(
            "any" to TimeRange(0..23999),
            "day" to TimeRange(23460..23999, 0..12541),
            "night" to TimeRange(12542..23459),
            "noon" to TimeRange(5000..6999),
            "midnight" to TimeRange(17000..18999),
            "dawn" to TimeRange(22300..23999, 0..999),
            "dusk" to TimeRange(11834..13701),
            "twilight" to TimeRange(11834..13701, 22300..23999, 0..166),
            "morning" to TimeRange(23000..23999, 0..4999),
            "afternoon" to TimeRange(7000..12999),
			"predawn" to TimeRange(19000..22999),
            "evening" to TimeRange(13000..16999)
        )
    }

    constructor() : super()
    constructor(vararg ranges: IntRange) : super(*ranges)
}