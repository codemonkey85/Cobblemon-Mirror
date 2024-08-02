/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.client.render.models.blockbench.blockentity.BlockEntityModel
import com.cobblemon.mod.common.util.cobblemonResource

/**
 * This is specifically for BlockEntities where we want to do more complex animation than what Java Edition already allows
 * Basically with Java Edition, we can only change the texture applied to a model, if we want to move bones around, need to
 * do something else, so we use this
 */
object BlockEntityModelRepository : VaryingModelRepository<BlockEntityModel>() {
    override val poserClass = BlockEntityModel::class.java
    override val title = "Block Entity"
    override val type = "block_entities"
    override val variationDirectories: List<String> = listOf("bedrock/$type/variations", "bedrock/$type")
    override val poserDirectories: List<String> = listOf("bedrock/$type/posers")
    override val modelDirectories: List<String> = listOf("bedrock/$type/models")
    override val animationDirectories: List<String> = listOf("bedrock/$type/animations")
    override val fallback = cobblemonResource("substitute")
    override val isForLivingEntityRenderer = false
    override fun registerInBuiltPosers() {}
}