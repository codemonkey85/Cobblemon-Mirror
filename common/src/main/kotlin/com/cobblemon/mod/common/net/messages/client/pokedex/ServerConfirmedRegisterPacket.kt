/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokedex

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokedex.PokedexLearnedInformation
import com.cobblemon.mod.common.client.net.pokedex.ServerConfirmedRegisterHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Send confirmation to client after new Pokédex registration
 *
 * Handled by [ServerConfirmedRegisterHandler]
 */
class ServerConfirmedRegisterPacket(
    val species: ResourceLocation,
    val newInformation: PokedexLearnedInformation
): NetworkPacket<ServerConfirmedRegisterPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(species)
        buffer.writeEnumConstant(newInformation)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) = ServerConfirmedRegisterPacket(buffer.readIdentifier(), buffer.readEnumConstant(PokedexLearnedInformation::class.java))
        val ID = cobblemonResource("server_confirmed_scan")
    }
}