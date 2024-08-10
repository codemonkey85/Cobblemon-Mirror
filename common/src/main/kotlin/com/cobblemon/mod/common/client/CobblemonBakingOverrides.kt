/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.BakingOverride
import com.cobblemon.mod.common.util.cobblemonModel
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

/**
 * The purpose of this class is to hold models that we want baked, but aren't associated with
 * any block state. Actual registration happens in ModelLoaderMixin/ModelLoader
 */
object CobblemonBakingOverrides {
    val models = mutableListOf<BakingOverride>()

    val RESTORATION_TANK_FLUID_BUBBLING = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_bubbling"),
        cobblemonModel("restoration_tank_fluid_bubbling", "none")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_1 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_1"),
        cobblemonModel("restoration_tank_fluid_chunked", "1")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_2 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_2"),
        cobblemonModel("restoration_tank_fluid_chunked", "2")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_3 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_3"),
        cobblemonModel("restoration_tank_fluid_chunked", "3")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_4 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_4"),
        cobblemonModel("restoration_tank_fluid_chunked", "4")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_5 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_5"),
        cobblemonModel("restoration_tank_fluid_chunked", "5")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_6 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_6"),
        cobblemonModel("restoration_tank_fluid_chunked", "6")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_7 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_7"),
        cobblemonModel("restoration_tank_fluid_chunked", "7")
    )
    val RESTORATION_TANK_FLUID_CHUNKED_8 = registerOverride(
        cobblemonResource("block/restoration_tank_fluid_chunked_8"),
        cobblemonModel("restoration_tank_fluid_chunked", "8")
    )
    val RESTORATION_TANK_CONNECTOR = registerOverride(
        cobblemonResource("block/restoration_tank_connector"),
        cobblemonModel("restoration_tank_connector", "none")
    )

    val COARSE_MULCH = registerOverride(
        cobblemonResource("block/coarse_mulch"),
        cobblemonModel("coarse_mulch", "none")
    )

    val GROWTH_MULCH = registerOverride(
        cobblemonResource("block/growth_mulch"),
        cobblemonModel("growth_mulch", "none")
    )

    val HUMID_MULCH = registerOverride(
        cobblemonResource("block/humid_mulch"),
        cobblemonModel("humid_mulch", "none")
    )

    val LOAMY_MULCH = registerOverride(
        cobblemonResource("block/loamy_mulch"),
        cobblemonModel("loamy_mulch", "none")
    )

    val PEAT_MULCH = registerOverride(
        cobblemonResource("block/peat_mulch"),
        cobblemonModel("peat_mulch", "none")
    )

    val RICH_MULCH = registerOverride(
        cobblemonResource("block/rich_mulch"),
        cobblemonModel("rich_mulch", "none")
    )

    val SANDY_MULCH = registerOverride(
        cobblemonResource("block/sandy_mulch"),
        cobblemonModel("sandy_mulch", "none")
    )

    val SURPRISE_MULCH = registerOverride(
        cobblemonResource("block/surprise_mulch"),
        cobblemonModel("surprise_mulch", "none")
    )

    fun registerOverride(modelLocation: Identifier, modelIdentifier: ModelIdentifier): BakingOverride {
        val result = BakingOverride(modelLocation, modelIdentifier)
        models.add(result)
        return result
    }
}