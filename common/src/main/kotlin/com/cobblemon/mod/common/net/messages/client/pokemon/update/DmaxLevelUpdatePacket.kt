/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Updates the Dynamax level of the Pokémon.
 *
 * @author Segfault Guy
 * @since July 27, 2023
 */
class DmaxLevelUpdatePacket(pokemon: () -> Pokemon, value: Int) : IntUpdatePacket<DmaxLevelUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun getSize() = IntSize.U_BYTE

    override fun set(pokemon: Pokemon, value: Int) {
        pokemon.dmaxLevel = value
    }

    companion object {
        val ID = cobblemonResource("dmax_level_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = DmaxLevelUpdatePacket(decodePokemon(buffer), buffer.readSizedInt(IntSize.U_BYTE))
    }
}