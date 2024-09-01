/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

/**
 * Event fired when a party [Pokemon] is sent out. Cancelling this event prevents a corresponding
 * [PokemonEntity] from being instantiated and spawned into the world.
 *
 * @author Segfault Guy
 * @since March 25th, 2023
 */
data class PokemonSentPreEvent(
    val pokemon: Pokemon,
    val level: ServerLevel,
    val position: Vec3
) : Cancelable()

/**
 * Event fired after a [PokemonEntity] is spawned from a player's party and after its animations are finished.
 * Only fired for party [Pokemon] sent out with animations.
 *
 * @author Segfault Guy
 * @since March 25th, 2023
 */
data class PokemonSentPostEvent(
    val pokemon: Pokemon,
    val pokemonEntity: PokemonEntity
)