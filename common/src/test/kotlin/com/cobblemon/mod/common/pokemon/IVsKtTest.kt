/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.junit.BootstrapMinecraft
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@BootstrapMinecraft
internal class IVsKtTest {

    @Test
    fun `should create a randomized set of IVs with 3 perfect values`() {
        val ivs = IVs.createRandomIVs(3)
        var foundPerfects = 0
        for ((_, value) in ivs) {
            if (value == IVs.MAX_VALUE) {
                foundPerfects++
            }
        }
        assertTrue(foundPerfects >= 3)
    }
}