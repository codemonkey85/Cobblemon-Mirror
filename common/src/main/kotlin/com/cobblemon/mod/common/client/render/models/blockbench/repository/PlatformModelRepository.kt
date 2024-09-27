/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.AncientPokeBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.BeastBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.PokeBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.RaftPlatformModel
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

object PlatformModelRepository : VaryingModelRepository<PosableModel>() {
    override val poserClass = PosableModel::class.java
    override val title = "Platform"
    override val type = "platforms"
    override val variationDirectories: List<String> = listOf("bedrock/$type/variations")
    override val poserDirectories: List<String> = listOf("bedrock/$type/posers")
    override val modelDirectories: List<String> = listOf("bedrock/$type/models")
    override val animationDirectories: List<String> = listOf("bedrock/$type/animations")
    override val isForLivingEntityRenderer = false

    override val fallback = cobblemonResource("substitute")

    override fun registerInBuiltPosers() {
        inbuilt("water_platform_m", ::RaftPlatformModel)
    }
}