/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

class CaughtBallUpdatePacket(pokemon: () -> Pokemon, value: PokeBall): SingleUpdatePacket<PokeBall, CaughtBallUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(this.value.name)
    }

    override fun set(pokemon: Pokemon, value: PokeBall) {
        pokemon.caughtBall = value
    }

    companion object {
        val ID = cobblemonResource("caught_ball_update")
        fun decode(buffer: RegistryFriendlyByteBuf): CaughtBallUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val pokeBall = PokeBalls.getPokeBall(buffer.readIdentifier()) ?: PokeBalls.POKE_BALL
            return CaughtBallUpdatePacket(pokemon, pokeBall)
        }
    }
}