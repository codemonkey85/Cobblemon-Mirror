/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Base packet class for moving a Pokémon from one position to another in the same store.
 *
 * @author Hiroku
 * @since June 18th, 2022
 */
abstract class MoveClientPokemonPacket<T : StorePosition, N : NetworkPacket<N>>(
    val storeID: UUID,
    val pokemonID: UUID,
    val newPosition: T
) : NetworkPacket<N> {
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(this.storeID)
        buffer.writeUUID(this.pokemonID)
        encodePosition(buffer, this.newPosition)
    }
    abstract fun encodePosition(buffer: RegistryFriendlyByteBuf, position: T)
}