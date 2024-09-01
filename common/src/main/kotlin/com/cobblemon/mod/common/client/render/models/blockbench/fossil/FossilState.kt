/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.fossil

import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState

/**
 * Floating state for a fossil Pokémon in the resurrection machine.
 *
 * @author Hiroku
 * @since October 30th, 2023
 */
class FossilState(startAge: Int = -1, startPartialTicks: Float = 0F) : PosableState() {
    var totalPartialTicks = 0F
    init {
        // generate phase offset if new
        age = if (startAge >= 0) startAge else (200F * Math.random()).toInt()
        currentPartialTicks = if(startAge > 0f) startPartialTicks else 0F
    }
    override fun getEntity() = null

    fun peekAge() : Int { // Need to find a way to do this with getAge that's not protected
        return this.age
    }

    // for dictating growth state of the Fossil Embryo
    var growthState = "Embryo"
    override fun updatePartialTicks(partialTicks: Float) {
        currentPartialTicks += partialTicks / 2
        totalPartialTicks += partialTicks / 2
    }

    override val schedulingTracker = SchedulingTracker()
}