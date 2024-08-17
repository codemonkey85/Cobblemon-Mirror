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