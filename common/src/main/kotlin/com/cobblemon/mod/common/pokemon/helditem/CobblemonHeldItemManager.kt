/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.helditem

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import java.util.function.Function

/**
 * The Cobblemon implementation of [HeldItemManager].
 * It directly consumes the [Pokemon.heldItem] when required.
 * The literal IDs are the path of item identifiers under the [Cobblemon.MODID] namespace.
 *
 * @author Licious
 * @since December 30th, 2022
 */
@Suppress("unused")
object CobblemonHeldItemManager : BaseCobblemonHeldItemManager() {

    /**
     * A collection of literal effect IDs that will trigger the Pokémon receiving the associated held item.
     */
    private val giveItemEffect = setOf("pickup", "recycle", "magician", "pickpocket", "thief", "covet", "harvest", "bestow", "switcheroo", "trick")

    /**
     * A collection of literal effect IDs that will trigger the Pokémon needing to have their item removed these are never communicated through '-enditem'.
     */
    private val takeItemEffect = setOf("magician", "pickpocket", "covet", "bestow")

    /** Remappings of [Item] to showdownId strings. */
    private val remaps = mutableMapOf<Item, String>()

    /** Remappings of [ItemStack] to showdownId strings. */
    private val stackRemaps = mutableListOf<Function<ItemStack, String?>>()

    override fun load() {
        super.load()
        Cobblemon.LOGGER.info("Imported {} held item IDs from showdown", this.loadedItemCount())
    }

    override fun showdownId(pokemon: BattlePokemon): String? {
        val itemStack = pokemon.effectedPokemon.heldItemNoCopy()
        if (remaps.containsKey(itemStack.item)) {
            return remaps[itemStack.item]
        }

        for (remap in stackRemaps) {
            val id = remap.apply(itemStack)
            if (id != null) {
                return id
            }
        }

        val original = super.showdownId(pokemon)
        if (original == null && pokemon.effectedPokemon.heldItemNoCopy().isEmpty) {
            // This will allow interactions such as thief to occur, we want this when there is no item only instead of overwriting other stacks that aren't held items.
            return ""
        }
        return original
    }

    fun showdownId(itemStack: ItemStack): String? {
        if (remaps.containsKey(itemStack.item)) {
            return remaps[itemStack.item]
        }

        for (remap in stackRemaps) {
            val id = remap.apply(itemStack)
            if (id != null) {
                return id
            }
        }

        return this.showdownIdOf(itemStack.item)
    }

    override fun handleStartInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {
        val itemID = battleMessage.effectAt(1)?.id ?: return
        val consumeHeldItems = this.shouldConsumeItem(pokemon, battle, itemID)
        if (battleMessage.hasOptionalArgument("silent")) {
            if (consumeHeldItems) {
                this.take(pokemon, itemID)
            }
            return
        }
        val effect = battleMessage.effect()
        val battlerName = pokemon.getName()
        // Airballoon is the only item using the null effect gimmick
        if (effect == null) {
            battle.broadcastChatMessage(battleLang("item.$itemID", battlerName))
            return
        }
        val sourceName = battleMessage.battlePokemonFromOptional(battle)?.getName() ?: Component.literal("UNKNOWN")
        val itemName = this.nameOf(itemID)
        val effectId = effect.id
        val text = when (effectId) {
            "magician", "pickpocket", "covet", "thief" -> battleLang("item.thief", battlerName, itemName, sourceName) // The "source" is actually the target here
            "pickup", "recycle" -> battleLang("item.recycle", battlerName, itemName)
            "switcheroo", "trick" -> battleLang("item.trick", battlerName, itemName)
            else -> battleLang("item.$effectId", battlerName, itemName, sourceName)
        }
        battle.broadcastChatMessage(text)
        // If it's a take and give effect, we don't want to follow through if we are not consuming held items
        if (this.takeItemEffect.contains(effectId) && this.giveItemEffect.contains(effectId) && !consumeHeldItems) {
            return
        }
        // Block item swapping in PVP until we have a rule
        if (battle.isPvP && !consumeHeldItems) {
            return
        }
        // if items aren't consumed, then we don't want to give them to wild pokemon (dupe)
        if (this.giveItemEffect.contains(effectId) && (pokemon.actor is PlayerBattleActor || consumeHeldItems)) {
            this.give(pokemon, itemID)
        }
        // allow players to steal wild held items
        if (this.takeItemEffect.contains(effectId) && (pokemon.actor !is PlayerBattleActor || consumeHeldItems)) {
            battleMessage.actorAndActivePokemonFromOptional(battle)?.second?.battlePokemon?.let { this.take(it, itemID) }
        }
    }

    override fun handleEndInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {
        val itemID = battleMessage.effectAt(1)?.id ?: return
        val consumeHeldItems = this.shouldConsumeItem(pokemon, battle, itemID)
        // These are sent when showdown wants the client to animate something but not produce any text
        if (battleMessage.hasOptionalArgument("silent")) {
            if (consumeHeldItems) this.take(pokemon, itemID)
            return
        }
        val battlerName = pokemon.getName()
        val itemName = this.nameOf(itemID)
        if (battleMessage.hasOptionalArgument("eat")) {
            battle.broadcastChatMessage(battleLang("item.eat", battlerName, itemName))
            if (consumeHeldItems) this.take(pokemon, itemID)
            return
        }
        val sourceName = battleMessage.battlePokemonFromOptional(battle)?.getName() ?: Component.literal("UNKNOWN")
        val effect = battleMessage.effect()
        val text = when {
            effect?.id != null -> battleLang("enditem.${effect.id}", battlerName, itemName, sourceName)
            else -> when (itemID) {
                "boosterenergy", "electricseed", "grassyseed", "mistyseed", "psychicseed", "roomservice", "blunderpolicy", "weaknesspolicy", "absorbbulb",
                    -> battleLang("enditem.generic", battlerName, itemName)   // TODO this cannot scale like this
                else
                    -> battleLang("enditem.$itemID", battlerName)
            }
        }
        if (consumeHeldItems) this.take(pokemon, itemID)
        battle.broadcastChatMessage(text)
    }

    override fun shouldConsumeItem(pokemon: BattlePokemon, battle: PokemonBattle, showdownId: String): Boolean {
        // In 3rd party and the future battles might have multiple types, give it a priority from pvp down to wild.
        val tag = when {
            battle.isPvP -> CobblemonItemTags.CONSUMED_IN_PVP_BATTLE
            battle.isPvN -> CobblemonItemTags.CONSUMED_IN_NPC_BATTLE
            else -> CobblemonItemTags.CONSUMED_IN_WILD_BATTLE
        }
        return pokemon.effectedPokemon.heldItem().`is`(tag)
    }

    /**
     * Registers a custom mapping from [Item] to showdown ID string.
     *
     * @param item The Minecraft [Item] instance that has a specific showdownId.
     * @param showdownId The showdown name of this item.
     */
    fun registerRemap(item: Item, showdownId: String) {
        this.remaps[item] = showdownId
    }

    /**
     * Registers a custom mapping from [ItemStack] to showdown ID string.
     *
     * @param remap A function that takes an [ItemStack] and returns the showdown name of this item or null if there was no match.
     */
    fun registerStackRemap(remap: Function<ItemStack, String?>) {
        this.stackRemaps.add(remap)
    }
}