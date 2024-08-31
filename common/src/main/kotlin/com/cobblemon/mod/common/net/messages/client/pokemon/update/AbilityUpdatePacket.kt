/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when the ability of a Pokémon has changed. Only sends the template.
 *
 * @author Hiroku
 * @since November 1st, 2022
 */
class AbilityUpdatePacket(pokemon: () -> Pokemon, ability: AbilityTemplate) : SingleUpdatePacket<AbilityTemplate, AbilityUpdatePacket>(pokemon, ability) {

    override val id = ID

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(this.value.name)
    }

    override fun set(pokemon: Pokemon, value: AbilityTemplate) {
        pokemon.ability = value.create()
    }

    companion object {
        val ID = cobblemonResource("ability_update")
        fun decode(buffer: RegistryFriendlyByteBuf): AbilityUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val ability = Abilities.get(buffer.readString())!!
            return AbilityUpdatePacket(pokemon, ability)
        }
    }

}