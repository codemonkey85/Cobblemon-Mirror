/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction

/**
 * Catch-all for instruction from Showdown that are not parsed.
 *
 * @author Hiroku
 * @since December 25th, 2023
 */
class IgnoredInstruction : InterpreterInstruction {
    override fun invoke(pokemonBattle: PokemonBattle) = Unit
}