/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import net.minecraft.tags.BlockTags

/**
 * Implements a [SpawningInfluence] that prevents Pokémon spawning on certain blocks.
 * This ensures that Pokémon don't spawn in areas that could disrupt player-built structures or mechanisms.
 * The influence checks the block directly above the potential spawn location; if the block is in the restricted list,
 * the spawn is disallowed.
 *
 * @author Aethen
 * @since February 20th, 2024
 */
open class RestrictedSpawnBlocksInfluence
    : SpawningInfluence {

    // The list of blocks that Pokémon are not allowed to spawn on.
    private val restrictedBlocks = BlockTags.RAILS



    /**
     * Affects the spawnable status of a Pokémon spawn detail.
     * If the block directly above the spawn location is in the restricted list, the spawn is disallowed.
     *
     * @param detail The Pokémon spawn detail to be affected.
     * @param ctx The context of the spawning event.
     * @return `true` if the spawn is allowed, `false` if it is disallowed.
     */
    override fun affectSpawnable(detail: SpawnDetail, ctx: SpawningContext): Boolean {
        // If the spawn detail is not a Pokémon, it is not affected by this influence.
        if (detail.type != "pokemon") return true

        // Get the position of the potential spawn location.
        val pokemonSpawnPos = ctx.position

        // Examine the block immediately above the spawn block to check for a rail
        // Potentially need a refactor if we ever want to check the spawn block itself.
        return ctx.world.getBlockState(pokemonSpawnPos.atY(pokemonSpawnPos.y + 1)).`is`(restrictedBlocks).not()
    }
}