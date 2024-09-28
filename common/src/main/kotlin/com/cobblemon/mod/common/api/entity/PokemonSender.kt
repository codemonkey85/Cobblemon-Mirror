/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.entity

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.concurrent.CompletableFuture

/**
 * Interface for custom entities that can send out and recall Pokémon. They get notified
 * when either of those actions are occurring.
 *
 * @author Hiroku
 * @since August 25th, 2023
 *
 */
interface PokemonSender {
    fun sendingOut(pokemon: Pokemon): CompletableFuture<Unit>
    fun recalling(pokemonEntity: PokemonEntity): CompletableFuture<Unit>
}