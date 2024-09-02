/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pasture

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.pasture.PasturePCGUIConfiguration
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonUnpasturedPacket
import net.minecraft.client.Minecraft

/**
 * Handles the request to unpasture a Pokemon to update the pasture GUI.
 *
 * @author Deltric
 * @since May 17th, 2023
 */
object PokemonUnpasturedHandler: ClientNetworkPacketHandler<PokemonUnpasturedPacket> {

    override fun handle(packet: PokemonUnpasturedPacket, client: Minecraft) {
        val pastureGuiConfiguration = (Minecraft.getInstance().screen as? PCGUI)?.configuration as? PasturePCGUIConfiguration
        pastureGuiConfiguration?.pasturedPokemon?.set(pastureGuiConfiguration.pasturedPokemon.get().filterNot { it.pokemonId == packet.pokemonId })
    }
}