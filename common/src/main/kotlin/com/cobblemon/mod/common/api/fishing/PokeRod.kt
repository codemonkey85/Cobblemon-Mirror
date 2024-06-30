/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.pokeball.PokeBall
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Base poke rod object
 * It is intended that there is one poke rod object initialized for a given poke rod type.
 *
 * @property name the poke rod registry name
 * @property pokeBallId The [Identifier] of the pokeball that is used as the bobber for this rod
 * @property lineColor list of [RGB] values that apply to the fishing line of the Pokérod
 */
data class PokeRod(
    val pokeBallId: Identifier,
    //Hex string of color
    val lineColor: String,
    var name: Identifier?
) {
    internal fun encode(buffer: PacketByteBuf) {
        buffer.writeIdentifier(name)
        buffer.writeIdentifier(pokeBallId)
        buffer.writeString(lineColor)
    }

    companion object {
        internal fun decode(buffer: PacketByteBuf): PokeRod {
            val name = buffer.readIdentifier()
            val pokeBallId = buffer.readIdentifier()
            val lineColor = buffer.readString()
            return PokeRod(pokeBallId, lineColor, name)
        }
    }

    fun getPokeBall(): PokeBall? {
        return PokeBalls.getPokeBall(pokeBallId)
    }
}