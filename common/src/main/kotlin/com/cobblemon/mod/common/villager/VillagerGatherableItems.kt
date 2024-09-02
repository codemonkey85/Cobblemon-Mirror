/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.villager

import com.cobblemon.mod.common.CobblemonItems
import net.minecraft.world.item.Item

object VillagerGatherableItems {
    val villagerGatherableItems = setOf<Item> (
            CobblemonItems.BLUE_MINT_SEEDS,
            CobblemonItems.CYAN_MINT_SEEDS,
            CobblemonItems.GREEN_MINT_SEEDS,
            CobblemonItems.PINK_MINT_SEEDS,
            CobblemonItems.RED_MINT_SEEDS,
            CobblemonItems.REVIVAL_HERB,
            CobblemonItems.WHITE_MINT_SEEDS,
            CobblemonItems.VIVICHOKE_SEEDS,
    )
}