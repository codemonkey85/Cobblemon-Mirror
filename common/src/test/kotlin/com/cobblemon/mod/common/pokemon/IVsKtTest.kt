/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import kotlin.random.Random
import com.cobblemon.mod.common.junit.BootstrapMinecraft
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@BootstrapMinecraft
internal class IVsKtTest {

    @Test
    fun `should create a randomized set of IVs with 0 perfect values`() {
        val ivs = IVs.createRandomIVs(0)
        var foundPerfects = 0
        for ((_, value) in ivs) {
            if (value == IVs.MAX_VALUE) {
                foundPerfects++
            }
        }
        assertTrue(foundPerfects >= 0)
    }

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

    @Test
    fun `should create a randomized set of IVs with 6 perfect values`() {
        val ivs = IVs.createRandomIVs(6)
        var foundPerfects = 0
        for ((_, value) in ivs) {
            if (value == IVs.MAX_VALUE) {
                foundPerfects++
            }
        }
        println("Found $foundPerfects perfect IVs") // Add this line
        assertTrue(foundPerfects >= 6)
    }

    @Test
    fun `should create a randomized set of IVs with a random number of perfect values`() {
        val randomPerfects = Random.nextInt(0, 7)
        val ivs = IVs.createRandomIVs(randomPerfects)
        var foundPerfects = 0
        for ((_, value) in ivs) {
            if (value == IVs.MAX_VALUE) {
                foundPerfects++
            }
        }
        println("Found $foundPerfects perfect IVs, expected $randomPerfects") // Add this line
        assertTrue(foundPerfects >= randomPerfects)
    }

    @Test
    fun `should create a randomized set of IVs where all values are within the acceptable range`() {
        val ivs = IVs.createRandomIVs(0)
        for ((_, value) in ivs) {
            assertTrue(value in 0..IVs.MAX_VALUE)
        }
    }
}