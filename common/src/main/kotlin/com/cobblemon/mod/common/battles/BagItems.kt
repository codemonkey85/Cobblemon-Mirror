/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.battles.BagItems.bagItems
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.item.battle.BagItemLike
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.item.ItemStack
import java.io.File

/**
 * A registry for [BagItem]s that could be parsed from [ItemStack]s. This registry is used as the resource loading
 * mechanism for bag item scripts, but add to [bagItems] to map from stacks to [BagItem]s.
 *
 * @author Hiroku
 * @since June 26th, 2023
 */
object BagItems : DataRegistry {
    override val id = cobblemonResource("bag_items")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<BagItems>()
    override fun sync(player: ServerPlayer) {}

    val bagItems = PrioritizedList<BagItemLike>()
    internal val bagItemsScripts = mutableMapOf<String, String>() // itemId to JavaScript

    init {
        observable.subscribe {
            Cobblemon.showdownThread.queue(ShowdownService::registerBagItems)
        }
    }

    fun getConvertibleForStack(stack: ItemStack): BagItemLike? {
        return bagItems.firstOrNull { it.getBagItem(stack) != null }
    }

    override fun reload(manager: ResourceManager) {
        manager.listResources("bag_items") { it.path.endsWith(".js") }.forEach { (identifier, resource) ->
            resource.open().use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    val js = reader.readText()
                    bagItemsScripts[resolvedIdentifier.path] = js
                }
            }
        }
        this.observable.emit(this)
    }
}