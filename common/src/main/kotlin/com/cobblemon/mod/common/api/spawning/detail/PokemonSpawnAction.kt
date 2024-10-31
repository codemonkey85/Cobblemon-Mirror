/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.feature.SeasonFeatureHandler
import com.cobblemon.mod.common.util.weightedSelection

/**
 * A [SpawnAction] that will spawn a single [PokemonEntity].
 *
 * @author Hiroku
 * @since February 13th, 2022
 */
class PokemonSpawnAction(
    ctx: SpawningContext,
    override val detail: PokemonSpawnDetail,
    /** The [PokemonProperties] that are about to be used. */
    var props: PokemonProperties = detail.pokemon.copy()
) : SingleEntitySpawnAction<PokemonEntity>(ctx, detail) {
    override fun createEntity(): PokemonEntity {
        if (props.species == null) LOGGER.error("PokemonSpawnAction run with null species - Spawn detail: ${detail.id}")
        if (props.level == null) {
            props.level = detail.getDerivedLevelRange().random()
        }
        val heldItems = detail.heldItems?.takeIf { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        val heldItem = if (heldItems.isNotEmpty()) {
            val until100 = 1 - heldItems.sumOf { it.percentage / 100 }
            if (until100 > 0 && ctx.world.random.nextDouble() < until100) {
                null
            } else {
                heldItems.weightedSelection { it.percentage }
            }
        } else {
            null
        }?.createStack(ctx)
        val entity = props.createEntity(ctx.world)
        entity.spawnCause = ctx.cause
        SeasonFeatureHandler.updateSeason(entity.pokemon, Cobblemon.seasonResolver(ctx.world, ctx.position))
        if (heldItem != null) {
            entity.pokemon.swapHeldItem(heldItem)
        }
        entity.drops = detail.drops
        // Useful debug code in situations where you want to find spawns
//        val fireworkRocketEntity = FireworkRocketEntity(ctx.world, ctx.position.x.toDouble(), ctx.position.y.toDouble() + 2, ctx.position.z.toDouble(), ItemStack(Items.FIREWORK_ROCKET))
//        ctx.world.spawnEntity(fireworkRocketEntity)
        return entity
    }
}