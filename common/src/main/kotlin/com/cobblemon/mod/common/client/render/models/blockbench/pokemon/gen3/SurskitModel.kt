/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class SurskitModel(root: ModelPart) : PosableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("surskit")
    override val head = getPart("head")

    override val portraitScale = 2.2F
    override val portraitTranslation = Vec3d(0.0, -0.9, 0.0)

    override val profileScale = 0.7F
    override val profileTranslation = Vec3d(0.0, 0.6, 0.0)

    lateinit var walk: Pose
    lateinit var standing: Pose
    lateinit var waterwalk: Pose
    lateinit var waterstand: Pose

    val wateroffset = -8

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("surskit", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            condition = { it.entity?.isTouchingWater == false },
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("surskit", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == false },
            idleAnimations = arrayOf(
                bedrock("surskit", "ground_walk")
            )
        )

        waterstand = registerPose(
            poseName = "water_stand",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.entity?.isTouchingWater == true },
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("surskit", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        waterwalk = registerPose(
            poseName = "water_walk",
            poseTypes = PoseType.MOVING_POSES,
            condition = { it.entity?.isTouchingWater == true },
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("surskit", "ground_walk")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )
    }
}