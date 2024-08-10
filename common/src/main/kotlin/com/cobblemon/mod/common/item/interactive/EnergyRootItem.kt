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
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.EnergyRootBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class EnergyRootItem(block: EnergyRootBlock, settings: Settings) : AliasedBlockItem(block, settings), PokemonSelectingItem {

    private val runtime = MoLangRuntime().setup()

    override val bagItem = object : BagItem {
        override val itemName = "item.cobblemon.energy_root"
        override fun canUse(battle: PokemonBattle, target: BattlePokemon) = target.health > 0 && target.health < target.maxHealth
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?): String {
            battlePokemon.effectedPokemon.decrementFriendship(CobblemonMechanics.remedies.getFriendshipDrop(runtime))
            return "potion ${getHealAmount()}"
        }
    }

    fun getHealAmount() = CobblemonMechanics.remedies.getHealingAmount("root", runtime, 150)

    override fun canUseOnPokemon(pokemon: Pokemon) = !pokemon.isFullHealth() && !pokemon.isFainted()

    override fun applyToPokemon(player: ServerPlayerEntity, stack: ItemStack, pokemon: Pokemon): TypedActionResult<ItemStack> {
        return if (this.canUseOnPokemon(pokemon)) {
            pokemon.currentHealth += this.getHealAmount()
            pokemon.decrementFriendship(CobblemonMechanics.remedies.getFriendshipDrop(runtime))
            player.playSound(CobblemonSounds.MEDICINE_HERB_USE, SoundCategory.PLAYERS, 1F, 1F)
            if (!player.isCreative)  {
                stack.decrement(1)
            }
            TypedActionResult.success(stack)
        } else {
            TypedActionResult.fail(stack)
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayerEntity, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        player.playSound(CobblemonSounds.MEDICINE_HERB_USE, SoundCategory.PLAYERS, 1F, 1F)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (user is ServerPlayerEntity) {
            return use(user, user.getStackInHand(hand))
        }
        return TypedActionResult.success(user.getStackInHand(hand))
    }

}
