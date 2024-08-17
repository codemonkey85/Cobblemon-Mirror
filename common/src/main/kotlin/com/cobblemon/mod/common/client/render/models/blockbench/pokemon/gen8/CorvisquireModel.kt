/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CorvisquireModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("corvisquire")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val head = getPart("head")


    override var portraitScale = 2.6F
    override var portraitTranslation = Vec3(-0.4, -0.6, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.25, 0.0)


    lateinit var stand: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("corvisquire", "cry") }

    override fun registerPoses() {

        val blink = quirk { bedrockStateful("corvisquire", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("corvisquire", "sleep"))
        )

        stand = registerPose(
            poseName = "standing",
            poseTypes = PoseType.SHOULDER_POSES + PoseType.UI_POSES + PoseType.STAND,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corvisquire", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.STAND - PoseType.UI_POSES - PoseType.SHOULDER_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corvisquire", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = PoseType.MOVING_POSES - PoseType.WALK,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corvisquire", "air_fly")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseType = PoseType.WALK,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corvisquire", "ground_walk")
            )
        )
    }
}