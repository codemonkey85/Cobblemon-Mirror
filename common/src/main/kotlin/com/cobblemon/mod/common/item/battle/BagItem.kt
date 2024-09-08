/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.BagItems
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer

/**
 * A bag item effect that links to a script in Showdown. Implementations must be added to
 * [BagItems.bagItems].
 *
 * @author Hiroku
 * @since June 26th, 2023
 */
interface BagItem {
    companion object {
        val EMPTY: BagItem = object : BagItem {
            override val itemName = "name"
            override val returnItem: Item = Items.AIR
            override fun canUse(battle: PokemonBattle, target: BattlePokemon) = true
            override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "none"
        }
    }

    /** The name provided to Showdown so that battle messages include the name of the effect for lang. */
    val itemName: String

    /** The return item given when the item is consumed. */
    val returnItem: Item

    /** Whether or not the item can probably be used right now, based on the mod-side version of battle state. */
    fun canUse(battle: PokemonBattle, target: BattlePokemon): Boolean
    /** Gets the itemId and data for inputting to Showdown. Hyper potion is `potion 200` for example. */
    fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?): String

    /**
     * Used for bag items that required prompts before selecting. It checks that the given stack is in the player's
     * hands, is non-zero on size, that the battle is still tolerant of a forced action for bag use, and that this
     * bag item can still be used.
     */
    fun canStillUse(player: ServerPlayer, battle: PokemonBattle, actor: BattleActor, target: BattlePokemon, stack: ItemStack): Boolean {
        return stack in player.handSlots && stack.count > 0 && canUse(battle, target) && actor.canFitForcedAction()
    }
}