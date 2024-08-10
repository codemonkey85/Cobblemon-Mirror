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
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Something that can be a bag item. This needs to be registered in [BagItems]
 *
 * @author Hiroku
 * @since June 26th, 2023
 */
interface BagItemConvertible {
    /**
     * Returns a [BagItem] if the given stack matches this convertible.
     *
     * If you're implementing this from an item subclass then you really
     * need to remember to check that the supplied stack is of the correct
     * item type because it doesn't necessarily have to be.
     *
     * [BagItemConvertible] can be implemented by non-Items so using it on
     * items is actually a bit weird, just convenient.
     */
    fun getBagItem(stack: ItemStack): BagItem?

    fun handleInteraction(player: ServerPlayerEntity, battlePokemon: BattlePokemon, stack: ItemStack): Boolean {
        val battle = battlePokemon.actor.battle
        val bagItem = getBagItem(stack) ?: return false
        if (!battlePokemon.actor.canFitForcedAction()) {
            player.sendMessage(battleLang("bagitem.cannot").red())
            return false
        }

        if (!bagItem.canUse(battle, battlePokemon)) {
            player.sendMessage(battleLang("bagitem.invalid").red())
            return false
        }

        battlePokemon.actor.forceChoose(BagItemActionResponse(bagItem, battlePokemon))
        stack.decrement(1)
        CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(battlePokemon.effectedPokemon.species.resourceIdentifier, Registries.ITEM.getId(stack.item)))
        return true
    }
}
