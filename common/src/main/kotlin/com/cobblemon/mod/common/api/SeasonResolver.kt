/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api

import com.cobblemon.mod.common.pokemon.feature.CobblemonSeason
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor

/**
 * Figures out what season it is at a particular position. Season is just a general approximation
 * used for things like Deerling variations.
 *
 * @author Hiroku
 * @since November 25th, 2022
 */
fun interface SeasonResolver {
    operator fun invoke(world: LevelAccessor, pos: BlockPos): CobblemonSeason?
}