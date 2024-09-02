/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.callback.MoveSelectCallbacks
import com.cobblemon.mod.common.api.callback.MoveSelectDTO
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BagItemActionResponse
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder

// TODO probably remove this? Still need to work through Ether logic and apply in other places

interface InteractOrBagItem {
    fun canUseOverworld(pokemon: Pokemon): Boolean
    fun canUseBattle(battlePokemon: BattlePokemon): Boolean

    fun getBagItem(stack: ItemStack): BagItem?

    fun onRegularUse(world: ServerLevel, user: ServerPlayer, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }



    fun onBattleUse(player: ServerPlayer, battlePokemon: BattlePokemon, stack: ItemStack): Boolean {
        val battle = battlePokemon.actor.battle

        val bagItem = getBagItem(stack) ?: return false
        if (!battlePokemon.actor.canFitForcedAction()) {
            player.sendSystemMessage(battleLang("bagitem.cannot").red())
            return false
        }

        if (!bagItem.canUse(battle, battlePokemon)) {
            player.sendSystemMessage(battleLang("bagitem.invalid").red())
            return false
        }

        val turn = battle.turn
        MoveSelectCallbacks.create(
            player = player,
            possibleMoves = battlePokemon.moveSet.map { move ->
                val enabled = move.currentPp < move.maxPp
                return@map MoveSelectDTO(move).also { it.enabled = enabled }
            }
        ) { _, _, move ->
            if (stack in player.handSlots && !stack.isEmpty && battlePokemon.actor.canFitForcedAction() && battle.turn == turn) {
                battlePokemon.actor.forceChoose(BagItemActionResponse(bagItem, battlePokemon, move.moveTemplate.name))
                if (!player.isCreative) {
                    stack.shrink(1)
                }
            }
        }

        return true
    }

    fun checkBattleItem(player: ServerPlayer, battle: PokemonBattle, actor: BattleActor, battlePokemon: BattlePokemon, stack: ItemStack, hand: InteractionHand): Boolean {

        val bagItem = getBagItem(stack) ?: return false

        if (!actor.canFitForcedAction()) {
            player.sendSystemMessage(battleLang("bagitem.cannot").red())
            return false
        }

        if (!bagItem.canUse(battle, battlePokemon)) {
            player.sendSystemMessage(battleLang("bagitem.invalid").red())
            return false
        }

        return player.getItemInHand(hand) === stack
    }
}