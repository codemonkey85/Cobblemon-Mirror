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
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent
import com.cobblemon.mod.common.api.item.HealingSource
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveFloat
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.level.Level
import net.minecraft.world.item.Items

class BerryJuiceItem : CobblemonItem(Properties()), PokemonSelectingItem, HealingSource {
    override val bagItem = object : BagItem {
        override val itemName = "item.cobblemon.berry_juice"
        override val returnItem = Items.BOWL
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "potion 20"
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) =  target.health < target.maxHealth && target.health > 0
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = !pokemon.isFullHealth() && pokemon.currentHealth > 0
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack>? {
        if (pokemon.isFullHealth()) {
            return InteractionResultHolder.fail(stack)
        }
        var amount = Integer.min(pokemon.currentHealth + 20, pokemon.maxHealth)
        CobblemonEvents.POKEMON_HEALED.postThen(PokemonHealedEvent(pokemon, amount, this), { cancelledEvent -> return InteractionResultHolder.fail(stack)}) { event ->
            amount = event.amount
        }
        pokemon.currentHealth = amount
        player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
        if (!player.isCreative)  {
            stack.shrink(1)
            val woodenBowlItemStack = ItemStack(Items.BOWL)
            if (!player.inventory.add(woodenBowlItemStack)) {
                // Drop the item into the world if the inventory is full
                player.drop(woodenBowlItemStack, false)
            }
        }
        return InteractionResultHolder.success(stack)
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
        if (!player.isCreative)  {
            val woodenBowlItemStack = ItemStack(Items.BOWL)
            if (!player.inventory.add(woodenBowlItemStack)) {
                // Drop the item into the world if the inventory is full
                player.drop(woodenBowlItemStack, false)
            }
        }
    }
}
