/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fossil

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.fossil.FossilRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.evolution.adapters.NbtItemPredicateAdapter
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.ItemLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object Fossils: JsonDataRegistry<Fossil> {

    override val id: ResourceLocation = cobblemonResource("fossils")
    override val type: PackType = PackType.SERVER_DATA
    override val observable = SimpleObservable<Fossils>()

    override val gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .registerTypeAdapter(NbtItemPredicate::class.java, NbtItemPredicateAdapter)
        .create()

    override val typeToken: TypeToken<Fossil> = TypeToken.get(Fossil::class.java)
    override val resourcePath: String = "fossils"

    private val fossils = hashMapOf<ResourceLocation, Fossil>()

    override fun reload(data: Map<ResourceLocation, Fossil>) {
        this.fossils.clear()
        data.forEach { (identifier, fossil) ->
            try {
                fossil.identifier = identifier
                this.fossils[identifier] = fossil
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Skipped loading the {} fossil", identifier, e)
            }
        }
        Cobblemon.LOGGER.info("Loaded {} fossils", this.fossils.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {
        FossilRegistrySyncPacket(this.all()).sendToPlayer(player)
    }

    /**
     * Gets all loaded [Fossil]s.
     */
    fun all() = this.fossils.values.toList()

    /**
     * Gets a [Fossil] by its [ResourceLocation].
     * @param identifier The identifier of the fossil.
     * @return The [Fossil] if loaded, otherwise null.
     */
    fun getByIdentifier(identifier: ResourceLocation): Fossil? = this.fossils[identifier]

    /**
     * Looks for a [Fossil] that matches a [ItemStack].
     * @param fossilStacks The fossil [ItemStack]'s.
     * @return The [Fossil] if found, otherwise null.
     */
    fun getFossilByItemStacks(fossilStacks: List<ItemStack>): Fossil? {
        return this.all().firstOrNull { it.matchesIngredients(fossilStacks) }
    }

    /**
     * Looks for a [Fossil] that is a superset of [ItemStack].
     * @param fossilStacks The fossil [ItemStack]'s.
     * @return The [Fossil] if found to be a superset, otherwise null.
     */
    fun getSubFossilByItemStacks(fossilStacks: List<ItemStack>): Fossil? {
        return this.all().firstOrNull { it.matchesIngredientsSubSet(fossilStacks) }
    }
    /**
     * Checks if a [ItemStack] is a fossil ingredient.
     * @param itemStack The ingredient [ItemStack].
     * @return true if it's a fossil ingredient, otherwise false.
     */
    fun isFossilIngredient(itemStack: ItemStack): Boolean {
        return this.all().any { it.isIngredient(itemStack) }
    }

}