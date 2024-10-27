/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.battles.instruction

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon

/**
 * Event that is fired when a Pokemon Terastallizes.
 * @param battle The Pokemon Battle.
 * @param pokemon The Pokemon that Terastallizes.
 * @param teraType The TeraType of the Terastallization.
 */
data class TerastallizationEvent(
    val battle: PokemonBattle,
    val pokemon: BattlePokemon,
    val teraType: TeraType
    )