/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.fishing.FishingBaitRegistrySyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.ItemStack

object FishingBaits : JsonDataRegistry<FishingBait>{
    override val id = cobblemonResource("fishing_baits")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<FishingBaits>()
    override val typeToken: TypeToken<FishingBait> = TypeToken.get(FishingBait::class.java)
    override val resourcePath = "fishing_baits"
    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .setPrettyPrinting()
        .create()

    private val itemMap = mutableMapOf<ResourceLocation, FishingBait>()

    override fun sync(player: ServerPlayer) {
        FishingBaitRegistrySyncPacket(this.itemMap.values.toList()).sendToPlayer(player)
    }

    override fun reload(data: Map<ResourceLocation, FishingBait>) {
        itemMap.clear()
        data.forEach { id, bait ->
            itemMap[bait.item] = bait
        }
    }

    fun getFromRodItemStack(stack: ItemStack): FishingBait? {
        return stack.components.get(CobblemonItemComponents.BAIT)?.bait
    }

    fun getFromBaitItemStack(stack: ItemStack): FishingBait? {
        return getFromIdentifier(BuiltInRegistries.ITEM.getKey(stack.item))
    }

    fun getFromIdentifier(identifier: ResourceLocation): FishingBait? {
        return itemMap[identifier]
    }

    fun isFishingBait(stack: ItemStack) = itemMap.containsKey(BuiltInRegistries.ITEM.getKey(stack.item))

//
//
//    fun getBaitSubcategory(item: ItemStack): String? {
//        val itemId = Registries.ITEM.getId(item.item)
//        if (itemId in itemMap.keys) {
//            return itemMap[itemId]?.subcategory
//        }
//        val tag = tagMap.keys.firstOrNull { it.fits(item.item, Registries.ITEM) }
//        if (tag != null) {
//            return tagMap[tag]?.subcategory
//        }
//        return null
//    }
//
//    fun getBaitSuccessChance(item: ItemStack): Double? {
//        val itemId = Registries.ITEM.getId(item.item)
//        if (itemId in itemMap.keys) {
//            return itemMap[itemId]?.chance
//        }
//        val tag = tagMap.keys.firstOrNull { it.fits(item.item, Registries.ITEM) }
//        if (tag != null) {
//            return tagMap[tag]?.chance
//        }
//        return null
//    }
//
//    fun getBaitEffectValue(item: ItemStack): Double? {
//        val itemId = Registries.ITEM.getId(item.item)
//        if (itemId in itemMap.keys) {
//            return itemMap[itemId]?.value
//        }
//        val tag = tagMap.keys.firstOrNull { it.fits(item.item, Registries.ITEM) }
//        if (tag != null) {
//            return tagMap[tag]?.value
//        }
//        return null
//    }
//
//
//    fun getBaitNote(item: ItemStack): String? {
//        val itemId = Registries.ITEM.getId(item.item)
//        if (itemId in itemMap.keys) {
//            return itemMap[itemId]?.note
//        }
//        val tag = tagMap.keys.firstOrNull { it.fits(item.item, Registries.ITEM) }
//        if (tag != null) {
//            return tagMap[tag]?.note
//        }
//        return null
//    }
}
