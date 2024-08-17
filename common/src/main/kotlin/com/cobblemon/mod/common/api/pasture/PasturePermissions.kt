/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pasture

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A set of permissions for using a pasture block.
 *
 * [canPasture] allows a player to pasture their own Pokémon in the block. If false, PC buttons do nothing.
 * [canUnpastureOthers] allows a player to unpasture Pokémon even if they do not own it.
 * [maxPokemon] is the largest number of Pokémon the player can put into the pasture.
 *
 * @author Hiroku
 * @since July 2nd, 2023
 */
class PasturePermissions(
    val canUnpastureOthers: Boolean,
    val canPasture: Boolean,
    val maxPokemon: Int
) {
    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) = PasturePermissions(
            canUnpastureOthers = buffer.readBoolean(),
            canPasture = buffer.readBoolean(),
            maxPokemon = buffer.readSizedInt(IntSize.SHORT)
        )
    }

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(canUnpastureOthers)
        buffer.writeBoolean(canPasture)
        buffer.writeSizedInt(IntSize.SHORT, maxPokemon)
    }
}