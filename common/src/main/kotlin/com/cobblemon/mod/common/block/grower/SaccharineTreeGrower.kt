/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.grower

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.world.feature.CobblemonConfiguredFeatures
import net.minecraft.block.sapling.SaplingGenerator
import net.minecraft.util.math.random.Random

class SaccharineTreeGrower() : SaplingGenerator() {
    override fun getTreeFeature(random: Random, bl: Boolean) = CobblemonConfiguredFeatures.SACCHARINE_TREE_KEY
}