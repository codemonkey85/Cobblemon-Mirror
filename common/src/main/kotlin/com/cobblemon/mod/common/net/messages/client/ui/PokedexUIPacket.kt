/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.ui

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.gui.PokedexUIPacketHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Tells the client to open the Pokédex interface.
 *
 * Handled by [PokedexUIPacketHandler].
 */
class PokedexUIPacket(val type: String, val initSpecies: ResourceLocation? = null): NetworkPacket<PokedexUIPacket> {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(type)
        buffer.writeNullable(initSpecies) { pb, value -> pb.writeIdentifier(value) }
    }

    companion object {
        val ID = cobblemonResource("pokedex_ui")

        fun decode(buffer: RegistryFriendlyByteBuf) = PokedexUIPacket(buffer.readString(), buffer.readNullable { it.readIdentifier() })
    }
}