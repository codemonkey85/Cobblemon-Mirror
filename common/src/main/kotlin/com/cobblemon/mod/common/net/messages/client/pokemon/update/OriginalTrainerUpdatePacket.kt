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
import net.minecraft.network.RegistryFriendlyByteBuf

class OriginalTrainerUpdatePacket(pokemon: () -> Pokemon, username: String?) : SingleUpdatePacket<String?, OriginalTrainerUpdatePacket>(pokemon, username) {
    override val id = ID

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(this.value) { _, v -> buffer.writeString(v) }
    }

    override fun set(pokemon: Pokemon, value: String?) {
        pokemon.originalTrainerName = value
    }

    companion object {
        val ID = cobblemonResource("original_trainer_update")
        fun decode(buffer: RegistryFriendlyByteBuf): OriginalTrainerUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val originalTrainer = buffer.readNullable { buffer.readString() }
            return OriginalTrainerUpdatePacket(pokemon, originalTrainer)
        }
    }
}