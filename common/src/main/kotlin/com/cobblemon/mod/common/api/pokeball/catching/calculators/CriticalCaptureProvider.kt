/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.calculators

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.CaughtCount
import net.minecraft.server.level.ServerPlayer
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * A provider for the possibility at a critical capture.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Critical_capture) page.
 *
 * @author Licious
 * @since January 29th, 2023
 */
interface CriticalCaptureProvider {

    /**
     * Checks if the critical capture should occur based on the Pokedex progression of the given [player].
     *
     * @param player The [ServerPlayer] being queried.
     * @param modifiedCatchRate The resulting catch rate calculated inside the [CaptureCalculator].
     * @return If the critical capture should occur
     */
    fun shouldHaveCriticalCapture(player: ServerPlayer, modifiedCatchRate: Float): Boolean {
        val caughtCount = Cobblemon.playerDataManager.getPokedexData(player).getGlobalCalculatedValue(CaughtCount)
        val caughtMultiplier = when {
            caughtCount <= 30 -> 0F
            // This one is exact
            caughtCount <= 150 -> 0.5F
            caughtCount <= 300 -> 1F
            caughtCount <= 450 -> 1.5F
            caughtCount <= 600 -> 2F
            else -> 2.5F
        }
        val b = modifiedCatchRate * caughtMultiplier
        // ToDo replace * 1F with * 2F when the Catching Charm is implemented and is active.
        val c = ((b * 1F) / 6F).roundToInt()
        return Random.nextInt(256) < c
    }

}