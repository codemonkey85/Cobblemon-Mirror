/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

/**
 * Collection of all AI properties definable at the species level of a Pokémon.
 *
 * @author Hiroku
 * @since July 15th, 2022
 */
open class PokemonBehaviour {
    val resting = RestBehaviour()
    var moving = MoveBehaviour()
    val idle = IdleBehaviour()
    val entityInteract = EntityBehaviour()
}