/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveInt
import kotlin.math.max
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * A berry that raises friendship but lowers EVs in a particular stat.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class FriendshipRaisingBerryItem(block: BerryBlock, val stat: Stat) : BerryItem(block), PokemonSelectingItem {
    override val bagItem = null
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.evs.getOrDefault(stat) > 0 || pokemon.friendship < Cobblemon.config.maxPokemonFriendship
    override fun applyToPokemon(
        player: ServerPlayerEntity,
        stack: ItemStack,
        pokemon: Pokemon
    ): TypedActionResult<ItemStack> {
        val friendshipRaiseAmount = genericRuntime.resolveInt(CobblemonMechanics.berries.friendshipRaiseAmount, pokemon)

        val increasedFriendship = pokemon.incrementFriendship(friendshipRaiseAmount)

        val currentStat = pokemon.evs.getOrDefault(stat)
        val newEV = max(currentStat - genericRuntime.resolveInt(CobblemonMechanics.berries.evLowerAmount), 0)
        pokemon.setEV(stat, newEV)
        val decreasedEVs = currentStat != pokemon.evs.getOrDefault(stat)

        return if (increasedFriendship || decreasedEVs) {
            player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
            if (!player.isCreative) {
                stack.decrement(1)
            }
            TypedActionResult.success(stack)
        } else {
            TypedActionResult.pass(stack)
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (world is ServerWorld && user is ServerPlayerEntity) {
            return use(user, user.getStackInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}