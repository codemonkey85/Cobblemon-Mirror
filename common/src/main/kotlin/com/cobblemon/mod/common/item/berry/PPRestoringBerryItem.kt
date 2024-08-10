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
import com.cobblemon.mod.common.api.item.PokemonAndMoveSelectingItem
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveInt
import kotlin.math.min
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * A berry that recovers some amount of a move's PP.
 *
 * @author Hiroku
 * @since August 5th, 2023
 */
class PPRestoringBerryItem(block: BerryBlock, val amount: () -> Expression): BerryItem(block), PokemonAndMoveSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${berry()!!.identifier.path}"
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = target.health > 0 && target.moveSet.any { it.currentPp < it.maxPp }
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "ether $data ${ genericRuntime.resolveInt(amount(), battlePokemon) }"
    }

    override fun canUseOnMove(move: Move) = move.currentPp < move.maxPp
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.moveSet.any(::canUseOnMove)
    override fun applyToPokemon(player: ServerPlayerEntity, stack: ItemStack, pokemon: Pokemon, move: Move) {
        val moveToRecover = pokemon.moveSet.find { it.template == move.template }
        if (moveToRecover != null && moveToRecover.currentPp < moveToRecover.maxPp) {
            moveToRecover.currentPp = min(moveToRecover.maxPp, moveToRecover.currentPp + genericRuntime.resolveInt(amount(), pokemon))
            player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
            if (!player.isCreative) {
                stack.decrement(1)
            }
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayerEntity, stack: ItemStack, battlePokemon: BattlePokemon, move: Move) {
        super.applyToBattlePokemon(player, stack, battlePokemon, move)
        player.playSound(CobblemonSounds.BERRY_EAT, SoundCategory.PLAYERS, 1F, 1F)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (world is ServerWorld && user is ServerPlayerEntity) {
            return use(user, user.getStackInHand(hand)) ?: TypedActionResult.pass(user.getStackInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}