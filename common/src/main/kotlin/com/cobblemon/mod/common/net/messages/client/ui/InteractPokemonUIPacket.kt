/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.ui

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.gui.InteractPokemonUIPacketHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Tells the client to open the Pokémon interaction interface.
 *
 * Handled by [InteractPokemonUIPacketHandler].
 *
 * @author Village
 * @since January 7th, 2023
 */
class InteractPokemonUIPacket(val pokemonID: UUID, val canMountShoulder: Boolean): NetworkPacket<InteractPokemonUIPacket> {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonID)
        buffer.writeBoolean(canMountShoulder)
    }

    companion object {
        val ID = cobblemonResource("interact_pokemon_ui")
        fun decode(buffer: RegistryFriendlyByteBuf) = InteractPokemonUIPacket(buffer.readUUID(), buffer.readBoolean())
    }
}