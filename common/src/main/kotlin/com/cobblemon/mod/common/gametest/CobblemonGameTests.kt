/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.gametest

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.pokemon.PokemonProperties.Companion.parse
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class CobblemonGameTests {

    @GameTest(template = "")
    fun spawnBidoof(context: GameTestHelper) {
        context.succeedWhen {
            val bidoof = parse("bidoof").create()
            bidoof.ivs[Stats.ATTACK] = 31

            val target = BlockPos(context.relativePos(BlockPos(0, 0, 0)))
            context.assertEntityPresent(CobblemonEntities.POKEMON)
            context.assertEntityData(target, CobblemonEntities.POKEMON, PokemonEntity::pokemon, bidoof)
        }
    }

}
