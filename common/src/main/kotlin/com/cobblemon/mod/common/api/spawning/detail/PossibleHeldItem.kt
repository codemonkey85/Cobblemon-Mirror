/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import net.minecraft.component.ComponentChanges
import net.minecraft.component.ComponentMap
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

class PossibleHeldItem(
    val item: String,
    val componentMap: ComponentMap? = null,
    val percentage: Double = 100.0
) {
    fun createStack(ctx: SpawningContext): ItemStack? {
        val itemRegistry = ctx.world.registryManager.get(RegistryKeys.ITEM)
        val item = if (item.startsWith("#")) {
            val tag = TagKey.of(RegistryKeys.ITEM, Identifier.of(item.substring(1)))

            val opt = itemRegistry.getEntryList(tag)
            if (opt.isPresent && opt.get().size() > 0) {
                val entryList = opt.get()
                entryList.getRandom(ctx.world.random).get().value()
            } else {
                LOGGER.error("Unable to find matching spawn held items for tag: $item")
                null
            }
        } else {
            itemRegistry.get(Identifier.of(item))?.takeIf { it != Items.AIR }
        } ?: return run {
            LOGGER.error("Unable to find matching spawn held item for ID: $item")
            null
        }

        val stack = ItemStack(item, 1)

        if (componentMap != null) {
            val componentBuilder = ComponentChanges.builder()
            componentMap.forEach {
                componentBuilder.add(it)
            }
            stack.applyChanges(componentBuilder.build())
        }

        return stack
    }
}