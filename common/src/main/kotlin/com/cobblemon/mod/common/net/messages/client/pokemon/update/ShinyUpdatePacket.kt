/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class ShinyUpdatePacket(pokemon: () -> Pokemon, value: Boolean) : BooleanUpdatePacket<ShinyUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun set(pokemon: Pokemon, value: Boolean) { pokemon.shiny = value }
    companion object {
        val ID = cobblemonResource("shiny_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = ShinyUpdatePacket(decodePokemon(buffer), buffer.readBoolean())
    }
}