/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokedex.scanning

import com.cobblemon.mod.common.pokedex.scanner.PokedexEntityData
import com.cobblemon.mod.common.pokedex.scanner.ScannableEntity
import net.minecraft.server.level.ServerPlayer

data class PokemonScannedEvent(val player: ServerPlayer, val scannedPokemonEntityData: PokedexEntityData, val scannedEntity: ScannableEntity) {
    val pokedexEntityData: PokedexEntityData
        get() = scannedPokemonEntityData

    val isOwned: Boolean
        get() = scannedPokemonEntityData.ownerUUID == player.uuid
}