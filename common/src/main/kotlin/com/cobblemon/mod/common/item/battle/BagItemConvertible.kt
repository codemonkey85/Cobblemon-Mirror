/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.battle

import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.PokemonInteractContext
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BagItemActionResponse
import com.cobblemon.mod.common.battles.BagItems
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Something that can be a bag item. This needs to be registered in [BagItems]
 *
 * @author Hiroku
 * @since June 26th, 2023
 */
interface BagItemLike {
    /**
     * Returns a [BagItem] if the given stack matches this convertible.
     *
     * If you're implementing this from an item subclass then you really
     * need to remember to check that the supplied stack is of the correct
     * item type because it doesn't necessarily have to be.
     *
     * [BagItemLike] can be implemented by non-Items so using it on
     * items is actually a bit weird, just convenient.
     */
    fun getBagItem(stack: ItemStack): BagItem?

    fun handleInteraction(player: ServerPlayer, battlePokemon: BattlePokemon, stack: ItemStack): Boolean {
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

        battlePokemon.actor.forceChoose(BagItemActionResponse(bagItem, battlePokemon))
        stack.shrink(1)
        CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(battlePokemon.entity!!.pokemon.species.resourceIdentifier, BuiltInRegistries.ITEM.getKey(stack.item)))
        return true
    }
}
