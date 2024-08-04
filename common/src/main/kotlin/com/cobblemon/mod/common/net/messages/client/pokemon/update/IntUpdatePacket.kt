/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A specific type of update for a Pokémon which updates a single integer value.
 *
 * This can be used for anything upper-bounded by an int, including shorts and bytes.
 *
 * @author Hiroku
 * @since November 28th, 2021
 */
abstract class IntUpdatePacket<T : NetworkPacket<T>>(pokemon: () -> Pokemon, value: Int) : SingleUpdatePacket<Int, T>(pokemon, value) {

    abstract fun getSize(): IntSize

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(this.getSize(), this.value)
    }
}