/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class WingullModel (root: ModelPart) : PosableModel(), BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("wingull")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")
    override val leftLeg = getPart("foot_left")
    override val rightLeg = getPart("foot_right")

    override val portraitScale = 3.0F
    override val portraitTranslation = Vec3d(-0.2, -2.8, 0.0)

    override val profileScale = 0.85F
    override val profileTranslation = Vec3d(0.0, 0.5, 0.0)

    lateinit var sleep: Pose
    lateinit var stand: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var water_surface_idle: Pose
    lateinit var water_surface_swim: Pose
    lateinit var water_surface_sleep: Pose

    val wateroffset = -9

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("wingull", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            condition = { it.entity?.isTouchingWater == false },
            idleAnimations = arrayOf(bedrock("wingull", "sleep"))
        )

        water_surface_sleep = registerPose(
            poseType = PoseType.SLEEP,
            condition = { it.entity?.isTouchingWater == true },
            idleAnimations = arrayOf(bedrock("wingull", "surfacewater_sleep")),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        stand = registerPose(
            poseName = "standing",
            poseTypes = PoseType.SHOULDER_POSES + PoseType.UI_POSES + PoseType.STATIONARY_POSES - PoseType.HOVER,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == false },
            idleAnimations = arrayOf(
                bedrock("wingull", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            condition = { it.entity?.isTouchingWater == false },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("pelipper", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            transformTicks = 10,
            condition = { it.entity?.isTouchingWater == false },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("pelipper", "air_fly")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            transformTicks = 10,
            condition = { it.entity?.isTouchingWater == false },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("wingull", "ground_walk")
            )
        )

        water_surface_idle = registerPose(
            poseName = "surface_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == true },
            idleAnimations = arrayOf(
                bedrock("wingull", "surfacewater_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        water_surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == true },
            idleAnimations = arrayOf(
                bedrock("wingull", "surfacewater_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )
    }
}