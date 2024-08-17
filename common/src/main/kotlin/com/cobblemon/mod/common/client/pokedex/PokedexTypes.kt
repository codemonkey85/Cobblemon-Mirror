/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.pokedex

import com.cobblemon.mod.common.util.cobblemonResource

enum class PokedexTypes {
    BLACK,
    BLUE,
    GREEN,
    PINK,
    RED,
    WHITE,
    YELLOW;

    fun getTexturePath() = cobblemonResource("textures/gui/pokedex/pokedex_base_${this.name.lowercase()}.png")
}