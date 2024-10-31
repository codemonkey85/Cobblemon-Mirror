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
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent
import com.cobblemon.mod.common.api.item.HealingSource
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveFloat
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

/**
 * A berry that heals the Pokémon by some portion of their max HP.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class PortionHealingBerryItem(block: BerryBlock, val canCauseConfusion: Boolean, val portion: () -> ExpressionLike): BerryItem(block), PokemonSelectingItem, HealingSource {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${this@PortionHealingBerryItem.berry()!!.identifier.path}"
        override val returnItem = Items.AIR
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?): String {
            val confuse = if (canCauseConfusion) berry()!!.dislikedBy(battlePokemon.nature) else false
            return "potion_by_portion ${genericRuntime.resolveFloat(portion(), battlePokemon)} $confuse"
        }
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) =  target.health < target.maxHealth && target.health > 0
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = !pokemon.isFainted() && !pokemon.isFullHealth()
    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack>? {
        if (pokemon.isFullHealth() || pokemon.isFainted()) {
            return InteractionResultHolder.fail(stack)
        }
        var amount = Integer.min(pokemon.currentHealth + (genericRuntime.resolveFloat(portion(), pokemon) * pokemon.maxHealth).toInt(), pokemon.maxHealth)
        CobblemonEvents.POKEMON_HEALED.postThen(PokemonHealedEvent(pokemon, amount, this), { cancelledEvent -> return InteractionResultHolder.fail(stack)}) { event ->
            amount = event.amount
        }
        pokemon.currentHealth = amount
        player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
        if (!player.isCreative) {
            stack.shrink(1)
        }
        return InteractionResultHolder.success(stack)
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}