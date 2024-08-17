/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.gui

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.interact.wheel.createPokemonInteractGui
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import net.minecraft.client.Minecraft

object InteractPokemonUIPacketHandler: ClientNetworkPacketHandler<InteractPokemonUIPacket> {
    override fun handle(packet: InteractPokemonUIPacket, client: Minecraft) {
        client.setScreen(createPokemonInteractGui(packet.pokemonID, packet.canMountShoulder))
    }
}