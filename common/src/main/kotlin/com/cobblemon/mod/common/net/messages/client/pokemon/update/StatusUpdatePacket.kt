/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readNullable
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeNullable
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf

class StatusUpdatePacket(pokemon: () -> Pokemon, value: PersistentStatus?): SingleUpdatePacket<PersistentStatus?, StatusUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(this.value) { pb, value -> pb.writeIdentifier(value.name) }
    }

    override fun set(pokemon: Pokemon, value: PersistentStatus?) {
        if (value == null) {
            pokemon.status = null
            return
        }
        pokemon.applyStatus(value)
    }

    companion object {
        val ID = cobblemonResource("status_update")
        fun decode(buffer: RegistryFriendlyByteBuf): StatusUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val identifier = buffer.readNullable(ByteBuf::readIdentifier) ?: return StatusUpdatePacket(pokemon, null)
            val status = Statuses.getStatus(identifier) as? PersistentStatus
            return StatusUpdatePacket(pokemon, status)
        }
    }
}