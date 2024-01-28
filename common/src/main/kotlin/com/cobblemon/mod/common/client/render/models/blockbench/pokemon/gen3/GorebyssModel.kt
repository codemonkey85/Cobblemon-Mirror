/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class GorebyssModel (root: ModelPart) : PosableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("gorebyss")
    override val head = getPart("head")

    override val portraitScale = 2.5F
    override val portraitTranslation = Vec3d(-1.2, -2.1, 0.0)

    override val profileScale = 0.8F
    override val profileTranslation = Vec3d(0.0, 0.2, 0.0)

    lateinit var standing: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("gorebyss", "ground_idle")
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("gorebyss", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("gorebyss", "water_idle"),
            )
        )
    }
}