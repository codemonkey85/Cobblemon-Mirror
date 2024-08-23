/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokedex

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class ServerConfirmedScanPacket(
    val prevKnowledge: PokedexEntryProgress,
    val newKnowledge: PokedexEntryProgress,
    val species: ResourceLocation
): NetworkPacket<ServerConfirmedScanPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeEnumConstant(prevKnowledge)
        buffer.writeEnumConstant(newKnowledge)
        buffer.writeIdentifier(species)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) = ServerConfirmedScanPacket(buffer.readEnumConstant(PokedexEntryProgress::class.java), buffer.readEnumConstant(PokedexEntryProgress::class.java), buffer.readIdentifier())

        val ID = cobblemonResource("server_confirmed_scan")
    }
}