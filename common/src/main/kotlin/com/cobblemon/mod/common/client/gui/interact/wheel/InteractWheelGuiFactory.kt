/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.wheel

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.interaction.PokemonInteractionGUICreationEvent
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.net.messages.server.battle.SpectateBattlePacket
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.net.messages.server.trade.AcceptTradeRequestPacket
import com.cobblemon.mod.common.net.messages.server.trade.OfferTradePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.util.*
import org.joml.Vector3f

fun createPokemonInteractGui(pokemonID: UUID, canMountShoulder: Boolean): InteractWheelGUI {
    val mountShoulder = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_shoulder.png"),
        tooltipText = "cobblemon.ui.interact.mount.shoulder",
        onPress = {
            if (canMountShoulder) {
                InteractPokemonPacket(pokemonID, true).sendToServer()
                closeGUI()
            }
        }
    )
    val giveItem = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_held_item.png"),
        tooltipText = "cobblemon.ui.interact.give.item",
        onPress = {
            InteractPokemonPacket(pokemonID, false).sendToServer()
            closeGUI()
        }
    )
    val options: Multimap<Orientation, InteractWheelOption> = ArrayListMultimap.create()
    options.put(Orientation.TOP_RIGHT, giveItem)
    if (canMountShoulder) {
        options.put(Orientation.TOP_LEFT, mountShoulder)
    }
    CobblemonEvents.POKEMON_INTERACTION_GUI_CREATION.post(PokemonInteractionGUICreationEvent(pokemonID, canMountShoulder, options))
    return InteractWheelGUI(options, Component.translatable("cobblemon.ui.interact.pokemon"))
}

fun createPlayerInteractGui(optionsPacket: PlayerInteractOptionsPacket): InteractWheelGUI {
    val trade = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_trade.png"),
        colour = { if (CobblemonClient.requests.tradeOffers.any { it.traderId == optionsPacket.targetId }) Vector3f(0F, 0.6F, 0F) else null },
        tooltipText = "cobblemon.ui.interact.trade",
        onPress = {
            val tradeOffer = CobblemonClient.requests.tradeOffers.find { it.traderId == optionsPacket.targetId }
            if (tradeOffer == null) {
                CobblemonNetwork.sendToServer(OfferTradePacket(optionsPacket.targetId))
            } else {
                CobblemonClient.requests.tradeOffers -= tradeOffer
                CobblemonNetwork.sendToServer(AcceptTradeRequestPacket(tradeOffer.tradeOfferId))
            }
            closeGUI()
        }
    )
    val battle = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_battle.png"),
        colour = { if (CobblemonClient.requests.battleChallenges.any { it.challengerId == optionsPacket.targetId }) Vector3f(0F, 0.6F, 0F) else null },
        tooltipText = "cobblemon.ui.interact.battle",
        onPress = {
            val battleRequest = CobblemonClient.requests.battleChallenges.find { it.challengerId == optionsPacket.targetId }
            // This can be improved in future with more detailed battle challenge data.
            BattleChallengePacket(optionsPacket.numericTargetId, optionsPacket.selectedPokemonId).sendToServer()
            closeGUI()
        }
    )
    val spectate = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_spectate_battle.png"),
        colour = { if (CobblemonClient.requests.battleChallenges.any { it.challengerId == optionsPacket.targetId }) Vector3f(0F, 0.6F, 0F) else null },
        onPress = {
            SpectateBattlePacket(optionsPacket.targetId).sendToServer()
            closeGUI()
        },
        tooltipText = "cobblemon.ui.interact.spectate"
        )
    val options: Multimap<Orientation, InteractWheelOption> = ArrayListMultimap.create()
    //The way things are positioned should probably be more thought out if more options are added
    optionsPacket.options.map {
        if (it.equals(PlayerInteractOptionsPacket.Options.TRADE)) {
            options.put(Orientation.TOP_LEFT, trade)
        }
        if (it.equals(PlayerInteractOptionsPacket.Options.BATTLE)) {
            options.put(Orientation.TOP_RIGHT, battle)
        }
        if (it.equals(PlayerInteractOptionsPacket.Options.SPECTATE_BATTLE)) {
            options.put(Orientation.TOP_RIGHT, spectate)
        }
    }
    return InteractWheelGUI(options, Component.translatable("cobblemon.ui.interact.player"))
}

private fun closeGUI() {
    Minecraft.getInstance().setScreen(null)
}