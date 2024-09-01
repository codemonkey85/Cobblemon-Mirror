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
import com.cobblemon.mod.common.client.gui.interact.battleRequest.BattleConfigureGUI
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.server.battle.SpectateBattlePacket
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.net.messages.server.trade.AcceptTradeRequestPacket
import com.cobblemon.mod.common.net.messages.server.trade.OfferTradePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getPlayer
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
    val activeBattleRequest = CobblemonClient.requests.battleChallenges.firstOrNull { it.challengerIds.contains(optionsPacket.targetId) }
    val activeTeamRequest = CobblemonClient.requests.multiBattleTeamRequests.firstOrNull { it.challengerIds.contains(optionsPacket.targetId) }
    val battle = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_battle.png"),
        secondaryIconResource =  if(activeBattleRequest != null|| activeTeamRequest != null)
            cobblemonResource("textures/gui/interact/icon_exclamation.png")
            else null,
        colour = { null },
        tooltipText = "cobblemon.ui.interact.battle",
        onPress = {
            Minecraft.getInstance().setScreen(BattleConfigureGUI(optionsPacket, activeBattleRequest, activeTeamRequest))
        }
    )

    val spectate = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_spectate_battle.png"),
        colour = { if (CobblemonClient.requests.battleChallenges.any { it.challengerIds.contains(optionsPacket.targetId) }) Vector3f(0F, 0.6F, 0F) else null },
        onPress = {
            SpectateBattlePacket(optionsPacket.targetId).sendToServer()
            closeGUI()
        },
        tooltipText = "cobblemon.ui.interact.spectate"
    )
    val options: Multimap<Orientation, InteractWheelOption> = ArrayListMultimap.create()
    //TODO: hasChallenge and hasTeamRequest get calculated a bunch of times. Might consider having the server just passing it over.
    val hasChallenge = CobblemonClient.requests.battleChallenges.any { it.challengerIds.contains(optionsPacket.targetId) }
    val hasTeamRequest = CobblemonClient.requests.multiBattleTeamRequests.any { it.challengerIds.contains(optionsPacket.targetId) }
    //The way things are positioned should probably be more thought out if more options are added
    var addBattleOption = false
    optionsPacket.options.forEach {
        if (it.equals(PlayerInteractOptionsPacket.Options.TRADE)) {
            options.put(Orientation.TOP_LEFT, trade)
        }
        if (!addBattleOption && (hasChallenge || hasTeamRequest || BattleConfigureGUI.battleRequestMap.containsKey(it))) {
            options.put(Orientation.TOP_RIGHT, battle)
            addBattleOption = true
        }
        if (it.equals(PlayerInteractOptionsPacket.Options.SPECTATE_BATTLE)) {
            if(!hasChallenge) {
                options.put(Orientation.TOP_RIGHT, spectate)
            }
        }
    }

    if (!addBattleOption && optionsPacket.targetId.getPlayer() != null) {
        // Player who is currently unable to battle
        options.put(Orientation.TOP_RIGHT, InteractWheelOption(
                iconResource = cobblemonResource("textures/gui/interact/icon_battle.png"),
                secondaryIconResource = null,
                colour = { Vector3f(0.5f, 0.5f, 0.5f) },
                tooltipText = "cobblemon.ui.interact.battle.unavailable",
                onPress = {}
        ))
    }

    return InteractWheelGUI(options, Component.translatable("cobblemon.ui.interact.player"))
}

private fun closeGUI() {
    Minecraft.getInstance().setScreen(null)
}