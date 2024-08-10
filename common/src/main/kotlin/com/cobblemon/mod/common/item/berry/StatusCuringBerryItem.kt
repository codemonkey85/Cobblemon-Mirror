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
import com.cobblemon.mod.common.api.pokemon.status.Status
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * A berry that cures statuses.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class StatusCuringBerryItem(block: BerryBlock, vararg val status: Status): BerryItem(block), PokemonSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${this@StatusCuringBerryItem.berry()!!.identifier.path}"
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = canUseOnPokemon(target.effectedPokemon)
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "cure_status${status.takeIf { it.isNotEmpty() }?.let { " ${it.joinToString(separator = " ") { it.showdownName } }" } ?: "" }"
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.status?.let { it.status in status || status.isEmpty() } == true && pokemon.currentHealth > 0
    override fun applyToPokemon(
        player: ServerPlayerEntity,
        stack: ItemStack,
        pokemon: Pokemon
    ): TypedActionResult<ItemStack>? {
        val currentStatus = pokemon.status?.status
        return if (currentStatus != null && (status.isEmpty() || currentStatus in status)) {
            pokemon.status = null
            player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
            if (!player.isCreative) {
                stack.decrement(1)
            }
            TypedActionResult.success(stack)
        } else {
            TypedActionResult.fail(stack)
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayerEntity, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (user is ServerPlayerEntity) {
            return use(user, user.getStackInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}