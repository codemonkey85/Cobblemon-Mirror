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
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent
import com.cobblemon.mod.common.api.item.HealingSource
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BagItemActionResponse
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import kotlin.math.ceil

/**
 * Item for reviving a Pokémon. Opens a party selection GUI.
 *
 * @author Hiroku
 * @since July 7th, 2023
 */
class ReviveItem(val max: Boolean): CobblemonItem(Properties()), HealingSource {
    val bagItem = object : BagItem {
        override val itemName = "item.cobblemon.${ if (max) "max_revive" else "revive" }"
        override val returnItem = Items.AIR
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = target.health <= 0
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "revive ${ if (max) "1" else "0.5" }"
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (world !is ServerLevel) {
            return InteractionResultHolder.success(user.getItemInHand(hand))
        } else {
            val player = user as ServerPlayer
            val stack = user.getItemInHand(hand)
            val battle = BattleRegistry.getBattleByParticipatingPlayer(player)
            if (battle != null) {
                val actor = battle.getActor(player)!!
                val battlePokemon = actor.pokemonList
                if (!actor.canFitForcedAction()) {
                    player.sendSystemMessage(battleLang("bagitem.cannot").red(), true)
                    return InteractionResultHolder.consume(stack)
                } else {
                    val turn = battle.turn
                    PartySelectCallbacks.createBattleSelect(
                        player = player,
                        pokemon = battlePokemon,
                        canSelect = { bagItem.canUse(battle, it) }
                    ) { bp ->
                        if (actor.canFitForcedAction() && bp.health <= 0 && battle.turn == turn && stack.isHeld(player)) {
                            player.playSound(CobblemonSounds.ITEM_USE, 1F, 1F)
                            actor.forceChoose(BagItemActionResponse(bagItem = bagItem, target = bp, data = bp.uuid.toString()))
                            if (!player.isCreative) {
                                stack.shrink(1)
                            }
                            CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(bp.effectedPokemon.species.resourceIdentifier, BuiltInRegistries.ITEM.getKey(stack.item)))
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
                        var amount = if (max) pk.maxHealth else ceil(pk.maxHealth / 2F).toInt()
                        CobblemonEvents.POKEMON_HEALED.postThen(PokemonHealedEvent(pk, amount, this), { cancelledEvent -> return@createFromPokemon }) { event ->
                            amount = event.amount
                        }
                        pk.currentHealth = amount
                        if (!player.isCreative) {
                            stack.shrink(1)
                        }
                        CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(pk.species.resourceIdentifier, BuiltInRegistries.ITEM.getKey(stack.item)))
                    }
                }
            }
            return InteractionResultHolder.success(stack)
        }
    }
}