/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.item.PokemonAndMoveSelectingItem
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveInt
import kotlin.math.min
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

/**
 * A berry that recovers some amount of a move's PP.
 *
 * @author Hiroku
 * @since August 5th, 2023
 */
class PPRestoringBerryItem(block: BerryBlock, val amount: () -> ExpressionLike): BerryItem(block), PokemonAndMoveSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${berry()!!.identifier.path}"
        override val returnItem = Items.AIR
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = target.health > 0 && target.moveSet.any { it.currentPp < it.maxPp }
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "ether $data ${ genericRuntime.resolveInt(amount(), battlePokemon) }"
    }

    override fun canUseOnMove(move: Move) = move.currentPp < move.maxPp
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.moveSet.any(::canUseOnMove)
    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon, move: Move) {
        val moveToRecover = pokemon.moveSet.find { it.template == move.template }
        if (moveToRecover != null && moveToRecover.currentPp < moveToRecover.maxPp) {
            moveToRecover.currentPp = min(moveToRecover.maxPp, moveToRecover.currentPp + genericRuntime.resolveInt(amount(), pokemon))
            player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
            if (!player.isCreative) {
                stack.shrink(1)
            }
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon, move: Move) {
        super.applyToBattlePokemon(player, stack, battlePokemon, move)
        player.playSound(CobblemonSounds.BERRY_EAT, 1F, 1F)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (world is ServerLevel && user is ServerPlayer) {
            return use(user, user.getItemInHand(hand)) ?: InteractionResultHolder.pass(user.getItemInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}