/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ClamperlModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("clamperl")
    override val head = getPart("head")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3(0.0, -2.15, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose
    lateinit var portrait: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES - PoseType.PORTRAIT + PoseType.STAND,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "ground_idle"),
                bedrock("clamperl", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseType = PoseType.FLOAT,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "water_swim"),
                bedrock("clamperl", "water_idle")
            )
        )

        portrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "portrait")
            )
        )
    }
}