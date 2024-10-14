/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokedex.scanning

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

data class PokemonScannedEvent(val player: ServerPlayer, val scannedPokemon: Pokemon) {
    val pokemon: Pokemon
        get() = scannedPokemon

    val isOwned: Boolean
        get() = pokemon.getOwnerPlayer() == player
}