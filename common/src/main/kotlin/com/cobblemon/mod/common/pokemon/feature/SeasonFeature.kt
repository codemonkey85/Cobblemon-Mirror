/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.SeasonResolver
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor
import java.util.*

/**
 * A season. You know the ones.
 *
 * @author Hiroku
 * @since November 25th, 2022
 */
enum class CobblemonSeason {
    SPRING,
    AUTUMN,
    SUMMER,
    WINTER;

    companion object {
        val ALL_VALUES = EnumSet.allOf(CobblemonSeason::class.java)
    }
}

const val SEASON = "season"

object SeasonFeatureHandler {
    fun updateSeason(pokemon: Pokemon, world: LevelAccessor, pos: BlockPos) {
        updateSeason(pokemon, Cobblemon.seasonResolver(world, pos))
    }

    fun updateSeason(pokemon: Pokemon, season: CobblemonSeason?) {
        val feature = pokemon.getFeature<StringSpeciesFeature>(SEASON) ?: return
        val currentSeason = feature.value
        val newSeason = season?.name?.lowercase()
        if (currentSeason != newSeason && newSeason != null) {
            feature.value = newSeason
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
    }
}

/**
 * A [SeasonResolver] that works by delegating the work to biome tags.
 *
 * @author Hiroku
 * @since November 25th, 2022
 */
object TagSeasonResolver : SeasonResolver {
    override fun invoke(world: LevelAccessor, pos: BlockPos): CobblemonSeason? {
        val biome = world.getBiome(pos)
        return if (biome.`is`(CobblemonBiomeTags.IS_WINTER)) {
            CobblemonSeason.WINTER
        } else if (biome.`is`(CobblemonBiomeTags.IS_SPRING)) {
            CobblemonSeason.SPRING
        } else if (biome.`is`(CobblemonBiomeTags.IS_AUTUMN)) {
            CobblemonSeason.AUTUMN
        } else if (biome.`is`(CobblemonBiomeTags.IS_SUMMER)) {
            CobblemonSeason.SUMMER
        } else {
            null
        }
    }
}