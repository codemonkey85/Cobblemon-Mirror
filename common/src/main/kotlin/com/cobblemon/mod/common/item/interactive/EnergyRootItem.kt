/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent
import com.cobblemon.mod.common.api.item.HealingSource
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.EnergyRootBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveInt
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class EnergyRootItem(block: EnergyRootBlock, settings: Properties) : ItemNameBlockItem(block, settings), PokemonSelectingItem, HealingSource {

    private val runtime = MoLangRuntime().setup()

    override val bagItem = object : BagItem {
        override val itemName = "item.cobblemon.energy_root"
        override val returnItem = Items.AIR
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = target.health > 0 && target.health < target.maxHealth
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?): String {
            battlePokemon.effectedPokemon.decrementFriendship(CobblemonMechanics.remedies.getFriendshipDrop(runtime))
            return "potion ${getHealAmount()}"
        }
    }

    fun getHealAmount() = CobblemonMechanics.remedies.getHealingAmount("root", runtime, 150)

    override fun canUseOnPokemon(pokemon: Pokemon) = !pokemon.isFullHealth() && !pokemon.isFainted()

    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon): InteractionResultHolder<ItemStack> {
        return if (this.canUseOnPokemon(pokemon)) {
            var amount = this.getHealAmount()
            CobblemonEvents.POKEMON_HEALED.postThen(PokemonHealedEvent(pokemon, amount, this), { cancelledEvent -> return InteractionResultHolder.fail(stack)}) { event ->
                amount = event.amount
            }
            pokemon.currentHealth += amount
            pokemon.decrementFriendship(CobblemonMechanics.remedies.getFriendshipDrop(runtime))
            pokemon.entity?.playSound(CobblemonSounds.MEDICINE_HERB_USE, 1F, 1F)
            if (!player.isCreative)  {
                stack.shrink(1)
            }
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.fail(stack)
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        battlePokemon.entity?.playSound(CobblemonSounds.MEDICINE_HERB_USE, 1F, 1F)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }

}
