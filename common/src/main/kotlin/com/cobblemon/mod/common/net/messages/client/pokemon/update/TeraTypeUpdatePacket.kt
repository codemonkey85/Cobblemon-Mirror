/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Updates the Tera Type of the Pokémon.
 *
 * @author Segfault Guy
 * @since July 19, 2023
 */
class TeraTypeUpdatePacket(pokemon: () -> Pokemon, value: TeraType) : SingleUpdatePacket<TeraType, TeraTypeUpdatePacket>(pokemon, value) {
    override val id = ID

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(value.id)
    }

    override fun set(pokemon: Pokemon, value: TeraType) {
        pokemon.teraType = value
    }

    companion object {
        val ID = cobblemonResource("tera_type_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = TeraTypeUpdatePacket(decodePokemon(buffer), TeraTypes.get(buffer.readIdentifier())!!)
    }
}