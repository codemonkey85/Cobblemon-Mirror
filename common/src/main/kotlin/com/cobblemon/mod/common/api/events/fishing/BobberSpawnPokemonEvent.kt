/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.fishing

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.item.ItemStack

/**
 * Event that is fired when a Pokemon is spawned by a bobber.
 */
interface BobberSpawnPokemonEvent {

    /**
     * Event that is fired before a Pokemon is spawned by a bobber.
     * @param bobber The PokeRodFishingBobberEntity that is spawning the Pokemon.
     * @param chosenBucket The bucket that is chosen.
     * @param rod The ItemStack of the rod that the bobber is attached to.
     */
    data class Pre(
        val bobber: PokeRodFishingBobberEntity,
        val chosenBucket: SpawnBucket,
        val rod: ItemStack
    ) : Cancelable(), BobberSpawnPokemonEvent

    /**
     * Event that is fired when a Pokemon is modified before it is spawned by a bobber.
     * @param chosenBucket The bucket that is chosen.
     * @param rod The ItemStack of the rod that the bobber is attached to.
     * @param pokemon The Pokemon that is modified.
     */
    data class Modify(
        val chosenBucket: SpawnBucket,
        val rod: ItemStack,
        val pokemon: PokemonEntity
    ) : BobberSpawnPokemonEvent

    /**
     * Event that is fired after a Pokemon is spawned by a bobber.
     * @param bobber The PokeRodFishingBobberEntity that is spawning the Pokemon.
     * @param chosenBucket The bucket that is chosen.
     * @param bait The ItemStack of the bait that is set on the rod.
     * @param pokemon The Pokemon that is spawned.
     */
    data class Post(
        val bobber: PokeRodFishingBobberEntity,
        val chosenBucket: SpawnBucket,
        val bait: ItemStack,
        val pokemon: PokemonEntity
    ) : BobberSpawnPokemonEvent
}