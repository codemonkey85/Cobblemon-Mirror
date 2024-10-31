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

/**
 * A provider for multiplier in the catch rate formula based on the number of species caught and registered to the Pokédex.
 *
 * @author Licious
 * @since January 29th, 2023
 */
interface PokedexProgressCaptureMultiplierProvider {

    /**
     * Resolves the multiplier based on the Pokedex progression of the given [player].
     *
     * @param player The [ServerPlayer] being queried.
     * @return The multiplier based on the caught count.
     */
    fun caughtMultiplierFor(player: ServerPlayer): Float {
        val caughtCount = Cobblemon.playerDataManager.getPokedexData(player).getGlobalCalculatedValue(CaughtCount)
        return when {
            caughtCount < 30 -> 1229F / 4096F
            // This one is exact
            caughtCount <= 150 -> 0.5F
            caughtCount <= 300 -> 2867F / 4096F
            caughtCount <= 450 -> 3277F / 4096F
            caughtCount <= 600 -> 3686F / 4096F
            else -> 1F
        }
    }

}