/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fossil

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.fossil.NaturalMaterialRegistrySyncPacket
import com.cobblemon.mod.common.registry.ItemTagCondition
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.ItemLikeConditionAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.ItemStack

object NaturalMaterials : JsonDataRegistry<List<NaturalMaterial>>{
    override val id = cobblemonResource("natural_materials")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<NaturalMaterials>()
    override val typeToken: TypeToken<List<NaturalMaterial>> =
        TypeToken.getParameterized(List::class.java, NaturalMaterial::class.java) as TypeToken<List<NaturalMaterial>>
    override val resourcePath = "natural_materials"
    override val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(ItemTagCondition::class.java, ItemLikeConditionAdapter)
        .create()

    private val itemMap = mutableMapOf<ResourceLocation, NaturalMaterial>()
    private val tagMap = mutableMapOf<ItemTagCondition, NaturalMaterial>()
    override fun sync(player: ServerPlayer) {
        NaturalMaterialRegistrySyncPacket(this.itemMap.values.toList() + this.tagMap.values.toList()).sendToPlayer(player)
    }

    override fun reload(data: Map<ResourceLocation, List<NaturalMaterial>>) {
        data.forEach { entry ->
            entry.value.forEach {
                itemMap.remove(it.item)
                if (it.item != null) {
                    itemMap[it.item] = it
                }
                if (it.tag != null) {
                    tagMap[it.tag] = it
                }
            }
        }
    }

    fun isNaturalMaterial(item: ItemStack): Boolean {
        val itemId = BuiltInRegistries.ITEM.getKey(item.item)
        return itemId in itemMap.keys || tagMap.keys.any {
            it.fits(item.item, BuiltInRegistries.ITEM)
        }
    }

    fun getContent(item: ItemStack): Int? {
        val itemId = BuiltInRegistries.ITEM.getKey(item.item)
        if (itemId in itemMap.keys) {
            return itemMap[itemId]?.content
        }
        val tag = tagMap.keys.firstOrNull { it.fits(item.item, BuiltInRegistries.ITEM) }
        if (tag != null) {
            return tagMap[tag]?.content
        }
        return null
    }

    fun getReturnItem(item: ItemStack): ResourceLocation? {
        val itemId = BuiltInRegistries.ITEM.getKey(item.item)
        if (itemId in itemMap.keys) {
            return itemMap[itemId]?.returnItem
        }
        val tag = tagMap.keys.firstOrNull { it.fits(item.item, BuiltInRegistries.ITEM) }
        if (tag != null) {
            return tagMap[tag]?.returnItem
        }
        return null
    }
}
