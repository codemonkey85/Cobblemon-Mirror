/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

// We do not need to know every single attribute as a client, as such, we only sync the aspects that matter
class SpeciesRegistrySyncPacket(species: Collection<Species>) : DataRegistrySyncPacket<Species, SpeciesRegistrySyncPacket>(species) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: Species) {
        try {
            buffer.writeIdentifier(entry.resourceIdentifier)
            entry.encode(buffer)
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception encoding the species {}", entry.resourceIdentifier, e)
        }
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Species? {
        val identifier = buffer.readIdentifier()
        val species = Species()
        species.resourceIdentifier = identifier
        return try {
            species.decode(buffer)
            species
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception decoding the species {}", identifier, e)
            null
        }
    }

    override fun synchronizeDecoded(entries: Collection<Species>) {
        PokemonSpecies.reload(entries.associateBy { it.resourceIdentifier })
    }

    companion object {
        val ID = cobblemonResource("species_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): SpeciesRegistrySyncPacket = SpeciesRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}