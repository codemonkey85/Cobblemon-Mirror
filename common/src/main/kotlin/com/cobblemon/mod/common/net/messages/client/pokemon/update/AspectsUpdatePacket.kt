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
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf

class AspectsUpdatePacket(pokemon: () -> Pokemon, value: Set<String>): SingleUpdatePacket<Set<String>, AspectsUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(this.value) { pb, value -> pb.writeString(value) }
    }

    override fun set(pokemon: Pokemon, value: Set<String>) {
        pokemon.forcedAspects = value
    }

    companion object {
        val ID = cobblemonResource("aspects_update")
        fun decode(buffer: RegistryFriendlyByteBuf): AspectsUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val aspects = buffer.readList(ByteBuf::readString).toSet()
            return AspectsUpdatePacket(pokemon, aspects)
        }
    }

}