/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer

/**
 * An item that will affect an [Entity].
 *
 * @param T The type of the [Entity] this item will affect.
 *
 * @param properties The [Item.Settings] of this item.
 */
interface InteractiveItem<T : Entity> {

    /**
     * Fired when a [ServerPlayer] interacts with the target entity.
     *
     * @param player The [ServerPlayer] interacting with the [entity].
     * @param entity The [Entity] of type [T] being interacted.
     * @param stack The [ItemStack] used in this interaction. [ItemStack.getItem] will always be of the same type as this [InteractiveItem].
     * @return true if the interaction was successful and no further interactions should be processed
     */
    fun onInteraction(player: ServerPlayer, entity: T, stack: ItemStack): Boolean

}