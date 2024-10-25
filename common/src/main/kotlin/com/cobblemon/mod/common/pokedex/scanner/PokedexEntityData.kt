/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import java.util.UUID

data class PokedexEntityData(
    val species: Species,
    val form: FormData,
    val gender: Gender,
    val aspects: Set<String>,
    val shiny: Boolean,
    val level: Int,
    val ownerUUID: UUID = UUID.randomUUID()
)