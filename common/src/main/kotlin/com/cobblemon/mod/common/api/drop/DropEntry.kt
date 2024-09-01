/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.drop

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

/**
 * Something that can be dropped from a [DropTable]. This does not strictly need to be an item.
 *
 * A new [DropEntry] subclass should be registered using [DropEntry.register].
 *
 * @author Hiroku
 * @since July 24th, 2022
 */
interface DropEntry {
    companion object {
        val entryTypes = mutableMapOf<String, Class<out DropEntry>>()
        var defaultType: Class<out DropEntry>? = null
        fun getByName(name: String) = entryTypes[name]
        fun <T : DropEntry> register(name: String, clazz: Class<T>, isDefault: Boolean = false) {
            entryTypes[name] = clazz
            if (isDefault) {
                defaultType = clazz
            }
        }
    }

    /**
     * The percentage chance of it being selected. If this value is 100, it is treated as guaranteed according to the
     * logic outline for [DropTable]. Other than that, a higher weight value means the drop entry will be more likely
     * to be dropped.
     */
    val percentage: Float
    /**
     * The drop's quantity when doing selection from the [DropTable]. See the drop table documentation for more on how
     * this works.
     */
    val quantity: Int
    /**
     * The maximum number of times the item can be selected drop the [DropTable]. In most cases this will just be 1,
     * meaning that the item, once picked, cannot be picked again. A larger value will allow the same [DropEntry] from
     * appearing multiple times in a drop action.
     */
    val maxSelectableTimes: Int
    /** The logic to use to actually drop the thing. */
    fun drop(entity: LivingEntity?, world: ServerLevel, pos: Vec3, player: ServerPlayer?)
}