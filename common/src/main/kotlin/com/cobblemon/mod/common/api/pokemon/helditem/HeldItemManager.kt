/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.helditem

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.helditem.BaseCobblemonHeldItemManager
import com.cobblemon.mod.common.pokemon.helditem.CobblemonEmptyHeldItemManager
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component

/**
 * Responsible for querying [BattlePokemon] for the presence of held items and their consumption.
 * These are invoked when creating battles and are only used for in battle data.
 * For operations on the [ItemStack] a [Pokemon] may be holding see [Pokemon.heldItem] and [Pokemon.swapHeldItem].
 * For the default implementation see [BaseCobblemonHeldItemManager], [CobblemonEmptyHeldItemManager] and [CobblemonHeldItemManager].
 *
 * @author Licious
 * @since December 30th, 2022
 */
interface HeldItemManager {

    /**
     * Queries the given [pokemon] for the presence of a held item.
     *
     * @param pokemon The [BattlePokemon] being queried for a held item.
     * @return The literal ID that showdown uses to represent this item such as 'abilityshield' if any.
     */
    fun showdownId(pokemon: BattlePokemon): String?

    /**
     * Queries the [Component] representation of the item under the given [showdownId].
     *
     * @param showdownId The literal ID of the held item on Showdown.
     * @return The [Component] representation.
     */
    fun nameOf(showdownId: String): Component

    /**
     * Invoked when an action instruction is sent from the Showdown server of type '-item'
     *
     * @param pokemon The [BattlePokemon] affected.
     * @param battle The [PokemonBattle] receiving the [battleMessage].
     * @param battleMessage The [BattleMessage] received.
     */
    fun handleStartInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage)

    /**
     * Invoked when an action instruction is sent from the Showdown server of type '-item'
     *
     * @param pokemon The [BattlePokemon] affected.
     * @param battle The [PokemonBattle] receiving the [battleMessage].
     * @param battleMessage The [BattleMessage] received.
     */
    fun handleEndInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage)

    /**
     * Gives the given [pokemon] a held item based on the [showdownId].
     *
     * @param pokemon The [BattlePokemon] being affected.
     * @param showdownId The literal ID of the held item on Showdown.
     */
    fun give(pokemon: BattlePokemon, showdownId: String)

    /**
     * Takes the given [showdownId] held item based from the [pokemon].
     *
     * @param pokemon The [BattlePokemon] being affected.
     * @param showdownId The literal ID of the held item on Showdown.
     */
    fun take(pokemon: BattlePokemon, showdownId: String)

    /**
     * Tests if the given [BattlePokemon] should have their item consumed.
     * This method is expected to be used in each implementation when needed.
     *
     * @param pokemon The [BattlePokemon] being tested.
     * @param battle The [PokemonBattle] the [pokemon] is participating in.
     * @param showdownId The literal ID of the held item on Showdown.
     */
    fun shouldConsumeItem(pokemon: BattlePokemon, battle: PokemonBattle, showdownId: String): Boolean = false

    companion object {

        /**
         * A [HeldItemManager] that never finds an item ID.
         * This is meant to be used when a battle Pokémon cannot be attached to any of the registered managers.
         * It will attempt to map a showdown item ID to a Cobblemon provided item and use that when taking or giving held items.
         * This is necessary for the basic functionality of consuming, swapping or removing items.
         */
        val EMPTY: HeldItemManager = CobblemonEmptyHeldItemManager

    }

}