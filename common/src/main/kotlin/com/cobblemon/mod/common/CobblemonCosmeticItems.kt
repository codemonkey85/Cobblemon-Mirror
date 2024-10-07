/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.CosmeticItemAssignmentSyncPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.cosmetic.CosmeticItemAssignment
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.ItemLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.PokemonPropertiesAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.Item

object CobblemonCosmeticItems : JsonDataRegistry<CosmeticItemAssignment> {
    override val id = cobblemonResource("cosmetic_items")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonCosmeticItems>()
    override val typeToken = TypeToken.get(CosmeticItemAssignment::class.java)
    override val resourcePath = "cosmetic_items"

    override val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(PokemonProperties::class.java, PokemonPropertiesAdapter(saveLong = false))
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()

    val cosmeticItems = mutableListOf<CosmeticItemAssignment>()

    override fun reload(data: Map<ResourceLocation, CosmeticItemAssignment>) {
        cosmeticItems.clear()
        cosmeticItems.addAll(data.values)
    }

    override fun sync(player: ServerPlayer) {
        CosmeticItemAssignmentSyncPacket(cosmeticItems).sendToPlayer(player)
    }

    fun findValidForPokemon(pokemon: Pokemon) = cosmeticItems.filter { it.pokemon.any { it.matches(pokemon) } }
}