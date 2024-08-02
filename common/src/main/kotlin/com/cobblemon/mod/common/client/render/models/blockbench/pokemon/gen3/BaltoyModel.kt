/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class BaltoyModel(root: ModelPart) : PokemonPosableModel(root), BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("baltoy")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")

    override var portraitScale = 3.1F
    override var portraitTranslation = Vec3(0.0, -1.6, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.3, 0.0)

    lateinit var walk: Pose
    lateinit var standing: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("baltoy", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("baltoy", "ground_walk")
            )
        )
    }
}