/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DratiniModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dratini")
    override val head = getPart("head")

    override var portraitScale = 1.66F
    override var portraitTranslation = Vec3(-0.48, 0.35, 0.0)

    override var profileScale = 0.58F
    override var profileTranslation = Vec3(0.14, 0.83, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var water_idle: CobblemonPose
    lateinit var water_swim: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var water_sleep: CobblemonPose
    lateinit var surface_swim: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dratini", "blink") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            animations = arrayOf(
                bedrock("dratini", "sleep")
            )
        )

        water_sleep = registerPose(
            poseName = "water_sleep",
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            animations = arrayOf(
                bedrock("dratini", "water_sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES - PoseType.FLOAT,
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dratini", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = MOVING_POSES - PoseType.SWIM,
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dratini", "ground_walk")
            )
        )

        water_idle = registerPose(
            poseName = "water_idle",
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dratini", "water_idle")
            )
        )

        water_swim = registerPose(
            poseName = "water_swim",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dratini", "water_swim")
            )
        )

        surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.MOVING_POSES,
            condition = { !it.isUnderWater && it.isInWater},
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dratini", "surfacewater_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dratini", "faint") else null
}