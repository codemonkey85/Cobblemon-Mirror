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
import net.minecraft.server.level.ServerPlayer

/**
 * Fired whenever a pokemon is about to receive a headpat that would increment it's friendship.
 * Cancelling will prevent this interaction from taking place
 */
data class PokemonHeadpatEvent(val pokemon: PokemonEntity, val player: ServerPlayer) : Cancelable()
