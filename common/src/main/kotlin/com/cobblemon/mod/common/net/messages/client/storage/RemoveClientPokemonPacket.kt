/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Removes a Pokémon from a particular store on the client side, working for both parties and PCs.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.RemoveClientPokemonHandler]
 *
 * @author Hiroku
 * @since June 18th, 2022
 */
class RemoveClientPokemonPacket internal constructor(val storeIsParty: Boolean, val storeID: UUID, val pokemonID: UUID) : NetworkPacket<RemoveClientPokemonPacket> {

    override val id = ID

    constructor(store: PokemonStore<*>, pokemonID: UUID): this(store is PartyStore, store.uuid, pokemonID)

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(storeIsParty)
        buffer.writeUUID(storeID)
        buffer.writeUUID(pokemonID)
    }

    companion object {
        val ID = cobblemonResource("remove_client_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = RemoveClientPokemonPacket(buffer.readBoolean(), buffer.readUUID(), buffer.readUUID())
    }
}