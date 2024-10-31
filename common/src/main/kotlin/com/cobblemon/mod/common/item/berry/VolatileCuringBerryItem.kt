/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Items

/**
 * A type of berry that cures a volatile status.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class VolatileCuringBerryItem(block: BerryBlock, val volatileStatus: String): BerryItem(block), PokemonSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${berry()!!.identifier.path}"
        override val returnItem = Items.AIR
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = true // When we track volatiles, can check for confusion
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "cure_volatile $volatileStatus"
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = false
    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon) = null
    override fun interactGeneral(player: ServerPlayer, stack: ItemStack) = InteractionResultHolder.pass(stack)

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
    }
}