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
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import java.util.UUID
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.joml.Vector3f

fun createPokemonInteractGui(pokemonID: UUID, canMountShoulder: Boolean, canRide: Boolean): InteractWheelGUI {
    val mountShoulder = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_shoulder.png"),
        tooltipText = "cobblemon.ui.interact.mount.shoulder",
        onPress = {
            if (canMountShoulder) {
                InteractPokemonPacket(pokemonID, true, false).sendToServer()
                closeGUI()
            }
        }
    )
    val giveItem = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_held_item.png"),
        tooltipText = "cobblemon.ui.interact.give.item",
        onPress = {
            InteractPokemonPacket(pokemonID, false, false).sendToServer()
            closeGUI()
        }
    )

    val ride = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_held_item.png"),
        tooltipText = "cobblemon.ui.interact.ride",
        onPress = {
            if (canRide) {
                InteractPokemonPacket(pokemonID, false, true).sendToServer()
                closeGUI()
            }
        }
    )

    val options: Multimap<Orientation, InteractWheelOption> = ArrayListMultimap.create()
    options.put(Orientation.TOP_RIGHT, giveItem)
    if(canRide) {
        options.put(Orientation.BOTTOM_LEFT, ride)
    }

    if (canMountShoulder) {
        options.put(Orientation.TOP_LEFT, mountShoulder)
    }

    CobblemonEvents.POKEMON_INTERACTION_GUI_CREATION.post(PokemonInteractionGUICreationEvent(pokemonID, canMountShoulder, options))
    return InteractWheelGUI(options, Component.translatable("cobblemon.ui.interact.pokemon"))
}

fun createPlayerInteractGui(optionsPacket: PlayerInteractOptionsPacket): InteractWheelGUI {
    val trade = InteractWheelOption(
        iconResource = cobblemonResource("textures/gui/interact/icon_trade.png"),
        secondaryIconResource =  if (CobblemonClient.requests.tradeOffers.any { it.traderId == optionsPacket.targetId })
            cobblemonResource("textures/gui/interact/icon_exclamation.png")
        else null,
        colour = { null },
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
        if (it.key == PlayerInteractOptionsPacket.Options.TRADE) {
            if (it.value == PlayerInteractOptionsPacket.OptionStatus.AVAILABLE) {
                options.put(Orientation.TOP_LEFT, trade)
            } else {
                val langKey = getLangKey(it.value)
                options.put(Orientation.TOP_LEFT, InteractWheelOption(
                        iconResource = cobblemonResource("textures/gui/interact/icon_trade.png"),
                        secondaryIconResource = null,
                        colour = { Vector3f(0.5f, 0.5f, 0.5f) },
                        tooltipText = langKey,
                        onPress = {}
                ))
            }
        }
        if (!addBattleOption && (hasChallenge || hasTeamRequest || BattleConfigureGUI.battleRequestMap.containsKey(it.key))) {
            if(it.value === PlayerInteractOptionsPacket.OptionStatus.AVAILABLE) {
                options.put(Orientation.TOP_RIGHT, battle)
                addBattleOption = true
            } else {
                options.put(Orientation.TOP_RIGHT, InteractWheelOption(
                        iconResource = cobblemonResource("textures/gui/interact/icon_battle.png"),
                        secondaryIconResource = null,
                        colour = { Vector3f(0.5f, 0.5f, 0.5f) },
                        tooltipText = getLangKey(it.value),
                        onPress = {}
                ))
            }
        }
        if (it.equals(PlayerInteractOptionsPacket.Options.SPECTATE_BATTLE)) {
            if(!hasChallenge) {
                options.put(Orientation.TOP_RIGHT, spectate)
            }
        }
    }

    return InteractWheelGUI(options, Component.translatable("cobblemon.ui.interact.player"))
}
private fun getLangKey(status: PlayerInteractOptionsPacket.OptionStatus) : String {
     return when (status) {
        PlayerInteractOptionsPacket.OptionStatus.TOO_FAR -> "cobblemon.ui.interact.too_far"
        PlayerInteractOptionsPacket.OptionStatus.INSUFFICIENT_POKEMON -> "cobblemon.battle.error.no_pokemon_opponent"
        else -> "cobblemon.ui.interact.unavailable"
    }
}


private fun closeGUI() {
    Minecraft.getInstance().setScreen(null)
}