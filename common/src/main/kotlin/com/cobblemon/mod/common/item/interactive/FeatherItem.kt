/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.level.Level

class FeatherItem(val stat: Stat) : CobblemonItem(Properties()), PokemonSelectingItem {

    companion object {
        const val EV_YIELD = 1
    }

    override val bagItem = null
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.evs.getOrDefault(stat) < EVs.MAX_STAT_VALUE

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack> {
        val evsGained = pokemon.evs.add(stat, EV_YIELD)
        return if (evsGained > 0) {
            pokemon.entity?.playSound(CobblemonSounds.MEDICINE_FEATHER_USE, 1F, 1F)
            if (!player.isCreative) {
                stack.shrink(1)
            }
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.fail(stack)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }
}