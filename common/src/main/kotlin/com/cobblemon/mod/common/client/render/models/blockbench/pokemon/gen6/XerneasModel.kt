/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class XerneasModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("xerneas")
    override val head = getPart("head")

    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3(-1.4, 4.4, 0.0)

    override var profileScale = 0.35F
    override var profileTranslation = Vec3(0.0, 1.2, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("xerneas", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walking",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("xerneas", "ground_walk")
                )
        )
    }
}