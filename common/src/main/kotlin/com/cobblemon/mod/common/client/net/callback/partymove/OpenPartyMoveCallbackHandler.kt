/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.callback.partymove

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.callback.PartySelectPokemonDTO
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.gui.interact.moveselect.MoveSelectConfiguration
import com.cobblemon.mod.common.client.gui.interact.moveselect.MoveSelectGUI
import com.cobblemon.mod.common.client.gui.interact.partyselect.PartySelectConfiguration
import com.cobblemon.mod.common.client.gui.interact.partyselect.PartySelectGUI
import com.cobblemon.mod.common.net.messages.client.callback.OpenPartyMoveCallbackPacket
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyMoveSelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyPokemonMoveSelectedPacket
import net.minecraft.client.Minecraft

object OpenPartyMoveCallbackHandler : ClientNetworkPacketHandler<OpenPartyMoveCallbackPacket> {
    override fun handle(packet: OpenPartyMoveCallbackPacket, client: Minecraft) {
        val pokemonToMoves = packet.pokemonList.toMap()
        val cancel: (Any) -> Unit = {
            CobblemonNetwork.sendToServer(PartyMoveSelectCancelledPacket(uuid = packet.uuid))
            if (it is MoveSelectGUI) {
                it.closeProperly()
            } else if (it is PartySelectGUI) {
                it.closeProperly()
            }
        }


        lateinit var partySelectConfiguration: PartySelectConfiguration

        fun makeMoveSelectConfiguration(pokemonSelectDTO: PartySelectPokemonDTO): MoveSelectConfiguration {
            return MoveSelectConfiguration(
                title = "".text(),
                moves = pokemonToMoves[pokemonSelectDTO]!!,
                onCancel = cancel,
                onBack = { Minecraft.getInstance().setScreen(PartySelectGUI(partySelectConfiguration)) },
                onSelect = { gui, moveSelectDTO ->
                    val pokemonIndex = packet.pokemonList.indexOfFirst { it.first == pokemonSelectDTO }
                    val moveIndex = pokemonToMoves[pokemonSelectDTO]!!.indexOf(moveSelectDTO)
                    CobblemonNetwork.sendToServer(PartyPokemonMoveSelectedPacket(packet.uuid, pokemonIndex, moveIndex))
                    gui.closeProperly()
                }
            )
        }

        partySelectConfiguration = PartySelectConfiguration(
            title = packet.partyTitle,
            pokemon = pokemonToMoves.keys.toList(),
            onCancel = cancel,
            onBack = cancel,
            onSelect = { _, it -> Minecraft.getInstance().setScreen(MoveSelectGUI(makeMoveSelectConfiguration(it))) }
        )

        Minecraft.getInstance().setScreen(PartySelectGUI(partySelectConfiguration))
    }
}