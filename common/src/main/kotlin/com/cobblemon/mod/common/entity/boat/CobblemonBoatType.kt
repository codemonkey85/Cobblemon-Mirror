/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.boat

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.WoodType

/**
 * Represents the various wooden boats in Cobblemon.
 *
 */
enum class CobblemonBoatType(val mountedOffset: Float) : StringRepresentable {

    APRICORN(-0.1f),
    SACCHARINE(-.01f);

    /**
     * The base [Item] form of this [CobblemonBoatType].
     */
    val boatItem: Item
        get() = when (this) {
            APRICORN -> CobblemonItems.APRICORN_BOAT
            SACCHARINE -> CobblemonItems.SACCHARINE_BOAT
    }

    /**
     * The base [Item] form of this [CobblemonBoatType] with a chest.
     */
    val chestBoatItem: Item get() = when (this) {
        APRICORN -> CobblemonItems.APRICORN_CHEST_BOAT
        SACCHARINE -> CobblemonItems.SACCHARINE_CHEST_BOAT
    }

    /**
     * The base [Block] form of this [CobblemonBoatType].
     */
    val baseBlock: Block get() = when (this) {
        APRICORN -> CobblemonBlocks.APRICORN_PLANKS
        SACCHARINE -> CobblemonBlocks.SACCHARINE_PLANKS
    }

    /**
     * The [WoodType] form of this [CobblemonBoatType].
     */
    val woodType: WoodType get() = when (this) {
        APRICORN -> CobblemonBlocks.APRICORN_WOOD_TYPE
        SACCHARINE -> CobblemonBlocks.SACCHARINE_WOOD_TYPE
    }


    override fun getSerializedName(): String = this.name.lowercase()

    companion object {

        internal fun ofOrdinal(ordinal: Int) = entries[ordinal]

    }

}