/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class FormUpdatePacket(pokemon: () -> Pokemon, form: FormData) : SingleUpdatePacket<FormData, FormUpdatePacket>(pokemon, form) {
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        this.value.encode(buffer)
    }

    override fun set(pokemon: Pokemon, value: FormData) {
        pokemon.form = value
    }

    override val id: ResourceLocation = ID

    companion object {
        val ID: ResourceLocation = cobblemonResource("packets/form-update")
        fun decode(buffer: RegistryFriendlyByteBuf): FormUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val form = FormData()
            form.species = pokemon().species
            form.decode(buffer)
            return FormUpdatePacket(pokemon, form)
        }
    }
}