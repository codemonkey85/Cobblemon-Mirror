/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.item

import com.cobblemon.mod.common.item.interactive.PotionItem

/**
 * Interface that represents a source of healing.
 */
interface HealingSource {
    /**
     * A source of healing that is forced, i.e it has no specific source.
     */
    object Force : HealingSource
}

