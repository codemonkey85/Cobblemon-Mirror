/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.misc

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.pokemon.HeldItemEvent
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

/**
 * Handles logic relating to Gimmighoul's Coin Stash and Scrap Stash bars.
 *
 * @author whatsy
 */
object GimmighoulStashHandler {

    val COIN_VALUE = 1
    val POUCH_VALUE = COIN_VALUE * 9
    val SACK_VALUE = POUCH_VALUE * 9

    val SCRAP_VALUE = 1
    val INGOT_VALUE = SCRAP_VALUE * 4
    val BLOCK_VALUE = INGOT_VALUE * 9
    fun interactMob(player: Player, hand: InteractionHand, pokemon:Pokemon) : Boolean {
        val itemStack = player.getItemInHand(hand)
        var success = false
        if(player is ServerPlayer && pokemon.getOwnerPlayer() == player) {
            success = handleItem(pokemon, itemStack.item)
            if(success) {
                itemStack.shrink(1)
            }
        }
        return success
    }

    fun giveHeldItem(event: HeldItemEvent.Post) {
        val pokemon = event.pokemon
        val item = event.received.item
        if(handleItem(pokemon, item)) {
            pokemon.removeHeldItem()
        }
    }
    fun handleItem(pokemon: Pokemon, item: Item) : Boolean {
        val goldHoard = pokemon.getFeature<IntSpeciesFeature>("gimmighoul_coins")
        val netheriteHoard = pokemon.getFeature<IntSpeciesFeature>("gimmighoul_netherite")
        if (goldHoard != null && goldHoard.value < 999) {
            val increase = when (item) {
                CobblemonItems.RELIC_COIN -> COIN_VALUE
                CobblemonItems.RELIC_COIN_POUCH -> POUCH_VALUE
                CobblemonItems.RELIC_COIN_SACK -> SACK_VALUE
                else -> 0
            }

            if (increase != 0) {
                goldHoard.value += increase
                if (goldHoard.value > 999) goldHoard.value = 999
                if (pokemon.entity != null) pokemon.entity!!.playSound(CobblemonSounds.GIMMIGHOUL_GIVE_ITEM, 1f, 1f)
                pokemon.markFeatureDirty(goldHoard)
                return true
            }
        }

        if (netheriteHoard != null && netheriteHoard.value < 256) {
            val increase = when (item) {
                Items.NETHERITE_SCRAP -> SCRAP_VALUE
                Items.NETHERITE_INGOT -> INGOT_VALUE
                Items.NETHERITE_BLOCK -> BLOCK_VALUE
                else -> 0
            }

            if (increase != 0) {
                netheriteHoard.value += increase
                if (netheriteHoard.value > 256) netheriteHoard.value = 256
                if (pokemon.entity != null) pokemon.entity!!.playSound(CobblemonSounds.GIMMIGHOUL_GIVE_ITEM, 1f, 1f)
                pokemon.markFeatureDirty(netheriteHoard)
                return true
            }
        }
        return false
    }

}