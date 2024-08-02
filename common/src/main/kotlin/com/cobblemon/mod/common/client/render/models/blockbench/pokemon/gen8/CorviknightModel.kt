/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CorviknightModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("corviknight")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val head = getPart("head")

    val openWingLeft = getPart("wing_open_left")
    val openWingRight = getPart("wing_open_right")
    val closedWingLeft = getPart("wing_closed_left")
    val closedWingRight = getPart("wing_closed_right")

    override var portraitScale = 1.0F
    override var portraitTranslation = Vec3(-0.45, 3.0, 0.0)

    override var profileScale = 0.36F
    override var profileTranslation = Vec3(0.0, 1.2, 0.0)


    lateinit var stand: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("corviknight", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("corviknight", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("corviknight", "sleep"))
        )

        stand = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES - PoseType.HOVER,
            transformTicks = 10,
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = false),
                openWingRight.createTransformation().withVisibility(visibility = false),
                closedWingLeft.createTransformation().withVisibility(visibility = true),
                closedWingRight.createTransformation().withVisibility(visibility = true)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corviknight", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = true),
                openWingRight.createTransformation().withVisibility(visibility = true),
                closedWingLeft.createTransformation().withVisibility(visibility = false),
                closedWingRight.createTransformation().withVisibility(visibility = false)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corviknight", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            transformTicks = 10,
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = true),
                openWingRight.createTransformation().withVisibility(visibility = true),
                closedWingLeft.createTransformation().withVisibility(visibility = false),
                closedWingRight.createTransformation().withVisibility(visibility = false)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corviknight", "air_fly")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            transformTicks = 10,
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = false),
                openWingRight.createTransformation().withVisibility(visibility = false),
                closedWingLeft.createTransformation().withVisibility(visibility = true),
                closedWingRight.createTransformation().withVisibility(visibility = true)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("corviknight", "ground_walk")
            )
        )
    }
}