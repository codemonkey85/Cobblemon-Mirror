/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MilceryModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("milcery")
    override val head = getPart("head")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3(-0.15, -2.8, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.2, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    lateinit var shoulderLeft: Pose
    lateinit var shoulderRight: Pose

    val shoulderOffset = 4

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES + PoseType.MOVING_POSES,
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("milcery", "ground_idle")
            )
        )
        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("milcery", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, shoulderOffset)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("milcery", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -shoulderOffset)
            )
        )
    }
}
