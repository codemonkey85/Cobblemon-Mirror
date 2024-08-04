/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.item

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.callback.MoveSelectCallbacks
import com.cobblemon.mod.common.api.callback.PartyMoveSelectCallbacks
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BagItemActionResponse
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.getBattleState
import com.cobblemon.mod.common.util.isHeld
import com.cobblemon.mod.common.util.isLookingAt
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB

/**
 * Interface to make it easier to define items that will, upon use, prompt you to
 * select a Pokémon and move for the item to be used on. This can be by interacting
 * with the Pokémon directly (which will skip the Pokémon selection screen) or by
 * interacting with air which will first show a Pokémon selection screen.
 *
 * @author Hiroku
 * @since July 29th, 2023
 */
interface PokemonAndMoveSelectingItem {
    fun use(player: ServerPlayer, stack: ItemStack): InteractionResultHolder<ItemStack>? {
        val entity = player.level()
            .getEntities(player, AABB.ofSize(player.position(), 16.0, 16.0, 16.0))
            .filter { player.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        player.getBattleState()?.let { (_, actor) ->
            if (bagItem == null) {
                return InteractionResultHolder.fail(stack)
            }
            if (!actor.canFitForcedAction()) {
                player.sendSystemMessage(battleLang("bagitem.cannot").red())
                return InteractionResultHolder.fail(stack)
            }
            val battlePokemon = actor.pokemonList.find { it.effectedPokemon == entity?.pokemon }
            if (entity == null) {
                return interactGeneralBattle(player, stack, actor)
            } else if (battlePokemon != null) {
                return interactWithSpecificBattle(player, stack, battlePokemon)
            }
        } ?: run {
            if (entity != null) {
                val pokemon = entity.pokemon
                if (entity.ownerUUID == player.uuid) {
                    return interactWithSpecific(player, stack, pokemon)
                } else {
                    return InteractionResultHolder.fail(stack)
                }
            } else {
                return interactGeneral(player, stack)
            }
        }

        return null
    }

    val bagItem: BagItem?
    fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon, move: Move)

    fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon, move: Move) {
        val battle = battlePokemon.actor.battle
        val bagItem = bagItem
        if (!battlePokemon.actor.canFitForcedAction()) {
            player.sendSystemMessage(battleLang("bagitem.cannot").red())
        } else if (!bagItem!!.canUse(battle, battlePokemon)) {
            player.sendSystemMessage(battleLang("bagitem.invalid").red())
        } else {
            battlePokemon.actor.forceChoose(BagItemActionResponse(bagItem, battlePokemon, move.template.name))
            if (!player.isCreative) {
                stack.shrink(1)
            }
        }
    }

    fun canUseOnPokemon(pokemon: Pokemon): Boolean
    fun canUseOnBattlePokemon(battlePokemon: BattlePokemon): Boolean = bagItem!!.canUse(battlePokemon.actor.battle, battlePokemon)
    fun canUseOnMove(pokemon: Pokemon, move: Move): Boolean = canUseOnMove(move)
    fun canUseOnMove(move: Move): Boolean

    fun interactWithSpecific(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon): InteractionResultHolder<ItemStack>? {

        if (player.isShiftKeyDown) {
            return InteractionResultHolder.pass(stack)
        }

        MoveSelectCallbacks.create(
            player = player,
            moves = pokemon.moveSet.toList(),
            canSelect = ::canUseOnMove,
            handler = { move -> if (stack.isHeld(player)) applyToPokemon(player, stack, pokemon, move) }
        )
        return InteractionResultHolder.success(stack)
    }

    fun interactWithSpecificBattle(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon): InteractionResultHolder<ItemStack>? {
        return if (canUseOnBattlePokemon(battlePokemon)) {
            MoveSelectCallbacks.create(
                player = player,
                moves = battlePokemon.moveSet.getMoves(),
                canSelect = ::canUseOnMove,
                handler = { applyToBattlePokemon(player, stack, battlePokemon, it) }
            )
            InteractionResultHolder.success(stack)
        } else {
            player.sendSystemMessage(battleLang("bagitem.invalid").red())
            InteractionResultHolder.fail(stack)
        }
    }

    fun interactGeneral(player: ServerPlayer, stack: ItemStack): InteractionResultHolder<ItemStack>? {
        PartyMoveSelectCallbacks.createFromPokemon(
            player = player,
            pokemon = player.party().toList(),
            canSelectPokemon = ::canUseOnPokemon,
            canSelectMove = ::canUseOnMove,
            handler = { pk, mv -> if (stack.isHeld(player)) applyToPokemon(player, stack, pk, mv) }
        )

        return InteractionResultHolder.success(stack)
    }

    fun interactGeneralBattle(player: ServerPlayer, stack: ItemStack, actor: BattleActor): InteractionResultHolder<ItemStack>? {
        PartyMoveSelectCallbacks.createFromPokemon(
            player = player,
            pokemon = actor.pokemonList.map { it.effectedPokemon },
            moves = { pk -> actor.pokemonList.find { it.effectedPokemon == pk }!!.moveSet.getMoves() },
            canSelectPokemon = { pk -> canUseOnBattlePokemon(actor.pokemonList.find { it.effectedPokemon == pk }!!) },
            canSelectMove = ::canUseOnMove,
            handler = { pk, mv -> applyToBattlePokemon(player, stack, actor.pokemonList.find { it.effectedPokemon == pk }!!, mv) }
        )

        return InteractionResultHolder.success(stack)
    }
}
