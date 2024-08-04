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
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class PossibleHeldItem(
    val item: String,
    val componentMap: DataComponentMap? = null,
    val percentage: Double = 100.0
) {
    fun createStack(ctx: SpawningContext): ItemStack? {
        val itemRegistry = ctx.world.registryAccess().registryOrThrow(Registries.ITEM)
        val item = if (item.startsWith("#")) {
            val tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(item.substring(1)))

            val opt = itemRegistry.getTag(tag)
            if (opt.isPresent && opt.get().size() > 0) {
                val entryList = opt.get()
                entryList.getRandomElement(ctx.world.random).get().value()
            } else {
                LOGGER.error("Unable to find matching spawn held items for tag: $item")
                null
            }
        } else {
            itemRegistry.get(ResourceLocation.parse(item))?.takeIf { it != Items.AIR }
        } ?: return run {
            LOGGER.error("Unable to find matching spawn held item for ID: $item")
            null
        }

        val stack = ItemStack(item, 1)

        if (componentMap != null) {
            val componentBuilder = DataComponentPatch.builder()
            componentMap.forEach {
                componentBuilder.set(it)
            }
            stack.applyComponents(componentBuilder.build())
        }

        return stack
    }
}