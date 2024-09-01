/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry.spawncondition

import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.Holder
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.Biome

class PreferredBiomeCondition(val minGroveSize: Int, val maxGroveSize: Int) : BerrySpawnCondition{

    override fun getGroveSize(random: RandomSource): Int {
        return random.nextIntBetweenInclusive(minGroveSize, maxGroveSize)
    }

    override fun canSpawn(berry: Berry, biome: Holder<Biome>): Boolean {
        val preferredBiomeTags = berry.preferredBiomeTags
        return preferredBiomeTags.any {biome.`is`(it)}
    }

    companion object {
        val ID = cobblemonResource("preferred_biome")
    }
}
