/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class LombreModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("lombre")
    override val head = getPart("head")

    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.4F
    override var portraitTranslation = Vec3(-0.15, -0.55, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.44, 0.0)

    lateinit var standing: Pose
    lateinit var waterstanding: Pose
    lateinit var walk: Pose
    lateinit var waterwalk: Pose
    lateinit var floating: Pose
    lateinit var swim: Pose
    lateinit var sleep: Pose

    val wateroffset = 1

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("lombre", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            quirks = arrayOf(blink),
            condition = { !it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lombre", "ground_idle")
            )
        )

        waterstanding = registerPose(
            poseName = "waterstanding",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            animations = arrayOf(
                bedrock("lombre", "water_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            condition = { !it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lombre", "ground_walk"),
            )
        )

        waterwalk = registerPose(
            poseName = "waterwalk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            animations = arrayOf(
                bedrock("lombre", "water_swim")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("lombre", "water_idle"),
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("lombre", "water_swim"),
            )
        )

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("lombre", "sleep"),
            )
        )

    }
    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("lombre", "faint") else null
}