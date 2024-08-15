/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.PokemonInteractContext
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.callback.PartySelectCallbacks
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BagItemActionResponse
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.isHeld
import com.cobblemon.mod.common.util.isInBattle
import com.cobblemon.mod.common.util.party
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.ceil

/**
 * Item for reviving a Pokémon. Opens a party selection GUI.
 *
 * @author Hiroku
 * @since July 7th, 2023
 */
class ReviveItem(val max: Boolean): CobblemonItem(Settings()) {
    val bagItem = object : BagItem {
        override val itemName = "item.cobblemon.${ if (max) "max_revive" else "revive" }"
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = target.health <= 0
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "revive ${ if (max) "1" else "0.5" }"
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (world !is ServerWorld) {
            return TypedActionResult.success(user.getStackInHand(hand))
        } else {
            val player = user as ServerPlayerEntity
            val stack = user.getStackInHand(hand)
            val battle = BattleRegistry.getBattleByParticipatingPlayer(player)
            if (battle != null) {
                val actor = battle.getActor(player)!!
                val battlePokemon = actor.pokemonList
                if (!actor.canFitForcedAction()) {
                    player.sendMessage(battleLang("bagitem.cannot").red(), true)
                    return TypedActionResult.consume(stack)
                } else {
                    val turn = battle.turn
                    PartySelectCallbacks.createBattleSelect(
                        player = player,
                        pokemon = battlePokemon,
                        canSelect = { bagItem.canUse(battle, it) }
                    ) { bp ->
                        if (actor.canFitForcedAction() && bp.health <= 0 && battle.turn == turn && stack.isHeld(player)) {
                            player.playSound(CobblemonSounds.ITEM_USE, SoundCategory.PLAYERS, 1F, 1F)
                            actor.forceChoose(BagItemActionResponse(bagItem = bagItem, target = bp, data = bp.uuid.toString()))
                            if (!player.isCreative) {
                                stack.decrement(1)
                            }
                            CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(bp.effectedPokemon.species.resourceIdentifier, Registries.ITEM.getId(stack.item)))
                        }
                    }
                }
            } else {
                val pokemon = player.party().toList()
                PartySelectCallbacks.createFromPokemon(
                    player = player,
                    pokemon = pokemon,
                    canSelect = Pokemon::isFainted
                ) { pk ->
                    if (pk.isFainted() && !player.isInBattle() && stack.isHeld(player)) {
                        pk.currentHealth = if (max) pk.hp else ceil(pk.hp / 2F).toInt()
                        if (!player.isCreative) {
                            stack.decrement(1)
                        }
                        CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(pk.species.resourceIdentifier, Registries.ITEM.getId(stack.item)))
                    }
                }
            }
            return TypedActionResult.success(stack)
        }
    }
}