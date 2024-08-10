/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveFloat
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * A berry that heals the Pokémon by some portion of their max HP.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class PortionHealingBerryItem(block: BerryBlock, val canCauseConfusion: Boolean, val portion: () -> Expression): BerryItem(block), PokemonSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${this@PortionHealingBerryItem.berry()!!.identifier.path}"
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?): String {
            val confuse = if (canCauseConfusion) berry()!!.dislikedBy(battlePokemon.nature) else false
            return "potion_by_portion ${genericRuntime.resolveFloat(portion(), battlePokemon)} $confuse"
        }
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) =  target.health < target.maxHealth && target.health > 0
    }

    override fun canUseOnPokemon(pokemon: Pokemon) = !pokemon.isFainted() && !pokemon.isFullHealth()
    override fun applyToPokemon(
        player: ServerPlayerEntity,
        stack: ItemStack,
        pokemon: Pokemon
    ): TypedActionResult<ItemStack>? {
        if (pokemon.isFullHealth() || pokemon.isFainted()) {
            return TypedActionResult.fail(stack)
        }

        pokemon.currentHealth = Integer.min(pokemon.currentHealth + (genericRuntime.resolveFloat(portion(), pokemon) * pokemon.hp).toInt(), pokemon.hp)
        player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
        if (!player.isCreative) {
            stack.decrement(1)
        }
        return TypedActionResult.success(stack)
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