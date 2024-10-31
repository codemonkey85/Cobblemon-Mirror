/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.status.Status
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.level.Level

/**
 * An item that cures statuses. Based on the [status] parameter it can be either specific [Status]es or all statuses (none specified).
 *
 * @author Hiroku
 * @since June 30th, 2023
 */
class StatusCureItem(val itemName: String, vararg val status: Status) : CobblemonItem(Properties()), PokemonSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName = this@StatusCureItem.itemName
        override val returnItem = Items.AIR
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = canUseOnPokemon(target.effectedPokemon)
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "cure_status${status.takeIf { it.isNotEmpty() }?.let { " ${it.joinToString(separator = " ") { it.showdownName } }" } ?: "" }"
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.status?.let { it.status in status || status.isEmpty() } == true && pokemon.currentHealth > 0
    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack>? {
        val currentStatus = pokemon.status?.status
        return if (currentStatus != null && (status.isEmpty() || currentStatus in status)) {
            pokemon.status = null
            pokemon.entity?.playSound(CobblemonSounds.MEDICINE_SPRAY_USE, 1F, 1F)
            if (!player.isCreative)  {
                stack.shrink(1)
                player.giveOrDropItemStack(ItemStack(bagItem.returnItem))
            }
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.fail(stack)
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        battlePokemon.entity?.playSound(CobblemonSounds.MEDICINE_SPRAY_USE, 1F, 1F)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }
}