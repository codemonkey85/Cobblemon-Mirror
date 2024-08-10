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
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.TypedActionResult

/**
 * A type of berry that cures a volatile status.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class VolatileCuringBerryItem(block: BerryBlock, val volatileStatus: String): BerryItem(block), PokemonSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${berry()!!.identifier.path}"
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = true // When we track volatiles, can check for confusion
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "cure_volatile $volatileStatus"
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = false
    override fun applyToPokemon(player: ServerPlayerEntity, stack: ItemStack, pokemon: Pokemon) = null
    override fun interactGeneral(player: ServerPlayerEntity, stack: ItemStack) = TypedActionResult.pass(stack)

    override fun applyToBattlePokemon(player: ServerPlayerEntity, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
    }
}