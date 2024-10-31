/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.helditem

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.google.common.collect.HashBiMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * The base Cobblemon implementation of an [HeldItemManager].
 * The literal IDs are the path of item identifiers under the [Cobblemon.MODID] namespace.
 *
 * @author Licious
 * @since January 6th, 2022
 */
abstract class BaseCobblemonHeldItemManager : HeldItemManager {

    private val itemIds = HashBiMap.create<String, Item>()

    internal open fun load() {
        this.itemIds.clear()
        val itemsJson = ShowdownService.service.getItemIds()
        val showdownIds = hashSetOf<String>()
        for (i in 0 until itemsJson.size()) {
            showdownIds += itemsJson[i].asString
        }
        BuiltInRegistries.ITEM.forEach { item ->
            val identifier = BuiltInRegistries.ITEM.getKey(item)
            if (identifier.namespace == Cobblemon.MODID) {
                val formattedPath = identifier.path.replace("_", "")
                if (showdownIds.contains(formattedPath)) {
                    this.itemIds[formattedPath] = item
                }
            }
        }
    }

    override fun showdownId(pokemon: BattlePokemon): String? {
        val item = pokemon.effectedPokemon.heldItemNoCopy()
        val identifier = item.get(CobblemonItemComponents.HELD_ITEM_REP)?.item ?: BuiltInRegistries.ITEM.getKey(item.item)

        val formattedPath = identifier.path.replace("_", "")
        if (this.itemIds.containsKey(formattedPath)) {
            return formattedPath
        }
        return null
    }

    override fun nameOf(showdownId: String): Component = this.itemIds[showdownId]?.description ?: Component.literal(showdownId)

    // This is safe to do as any item triggers will only happen if a Pokémon has a valid held item to begin with.
    override fun give(pokemon: BattlePokemon, showdownId: String) {
        val stack = this.itemIds[showdownId]?.let { ItemStack(it) } ?: ItemStack.EMPTY
        pokemon.effectedPokemon.swapHeldItem(stack, false)
    }

    // This is safe to do as any item triggers will only happen if a Pokémon has a valid held item to begin with.
    override fun take(pokemon: BattlePokemon, showdownId: String) {
        pokemon.effectedPokemon.removeHeldItem()
    }

    /**
     * Returns the amount of loaded item IDs.
     *
     * @return The amount of loaded item IDs.
     */
    protected fun loadedItemCount() = this.itemIds.size

    /**
     * Find the Showdown literal ID of the given [item].
     * This only works for items under the [Cobblemon.MODID] namespace.
     * If you wish to support your own items you need to implement your own [HeldItemManager].
     *
     * @param item The [Item] being queried.
     * @return The literal Showdown ID if any.
     */
    fun showdownIdOf(item: Item): String? {
        val identifier = BuiltInRegistries.ITEM.getKey(item)
        val formattedPath = identifier.path.replace("_", "")
        if (this.itemIds.containsKey(formattedPath)) {
            return formattedPath
        }
        return null
    }
}