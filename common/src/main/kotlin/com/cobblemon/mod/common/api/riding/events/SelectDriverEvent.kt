/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.events

import net.minecraft.world.entity.LivingEntity

data class SelectDriverEvent(val options: Set<LivingEntity>) {

    private var result: DriverSuggestion? = null

    fun result(): LivingEntity? {
        return this.result?.entity
    }

    /**
     * Suggests a particular living entity as a potential driver. Only non-negative
     * values are permitted by this function. All priority values with lesser priorities
     * will be outright ignored.
     */
    fun suggest(target: LivingEntity, priority: Int) {
        if (priority >= 0 && priority > (this.result?.priority ?: -1)) {
            this.result = DriverSuggestion(target, priority)
        }
    }

    data class DriverSuggestion(val entity: LivingEntity, val priority: Int)
}