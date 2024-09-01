/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry.spawncondition

import com.cobblemon.mod.common.api.berry.Berry
import net.minecraft.core.Holder
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.Biome

interface BerrySpawnCondition {
    fun getGroveSize(random: RandomSource): Int

    fun canSpawn(berry: Berry, biome: Holder<Biome>): Boolean
}
