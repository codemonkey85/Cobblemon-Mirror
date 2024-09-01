/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokeball

import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity

/**
 * Event fired when a capture calculator is accessing a Pokémon's catch rate. The [catchRate] property can
 * be changed to adjust the rate.
 *
 * If you want to prevent a Pokémon from being caught, you should be handling the [PokeBallCaptureCalculatedEvent]
 * event instead.
 *
 * @author Hiroku
 * @since August 20th, 2023
 */
class PokemonCatchRateEvent(
    val thrower: LivingEntity,
    val pokeBallEntity: EmptyPokeBallEntity,
    val pokemonEntity: PokemonEntity,
    var catchRate: Float
)