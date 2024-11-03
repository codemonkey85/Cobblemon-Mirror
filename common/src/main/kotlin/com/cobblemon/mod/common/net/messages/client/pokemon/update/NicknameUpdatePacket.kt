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
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeText
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent

class NicknameUpdatePacket(pokemon: () -> Pokemon, value: MutableComponent?): SingleUpdatePacket<MutableComponent?, NicknameUpdatePacket>(pokemon, value) {
    override val id = ID

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(value) { _, v -> buffer.writeText(v) }
    }

    override fun set(pokemon: Pokemon, value: MutableComponent?) { pokemon.nickname = value }

    companion object {
        val ID = cobblemonResource("nickname_update")
        fun decode(buffer: RegistryFriendlyByteBuf): NicknameUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val nickname = buffer.readNullable { buffer.readText().copy() }
            return NicknameUpdatePacket(pokemon, nickname)
        }
    }

}