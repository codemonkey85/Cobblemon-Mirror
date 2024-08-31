/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet used for when EVs have changed.
 *
 * @author Hiroku
 * @since November 23rd, 2022
 */
class EVsUpdatePacket(pokemon: () -> Pokemon, eVs: EVs) : SingleUpdatePacket<EVs, EVsUpdatePacket>(pokemon, eVs) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        this.value.saveToBuffer(buffer)
    }
    override fun set(pokemon: Pokemon, value: EVs) {
        value.forEach { (stat, value) ->
            pokemon.evs[stat] = value
        }
    }
    companion object {
        val ID = cobblemonResource("ev_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = EVsUpdatePacket(decodePokemon(buffer), EVs().apply { loadFromBuffer(buffer) })
    }
}

/**
 * Packet used for when IVs have changed.
 *
 * @author Hiroku
 * @since November 23rd, 2022
 */
class IVsUpdatePacket(pokemon: () -> Pokemon, iVs: IVs) : SingleUpdatePacket<IVs, IVsUpdatePacket>(pokemon, iVs) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        this.value.saveToBuffer(buffer)
    }
    override fun set(pokemon: Pokemon, value: IVs) {
        value.forEach { (stat, value) ->
            pokemon.ivs[stat] = value
        }
    }
    companion object {
        val ID = cobblemonResource("iv_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = IVsUpdatePacket(decodePokemon(buffer), IVs().apply { loadFromBuffer(buffer) })
    }
}