/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WingFlapIdleAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class UnfezantModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("unfezant")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val head = getPart("head_ai")

    val openWingLeft = getPart("open_left")
    val openWingRight = getPart("open_right")
    val closedWingLeft = getPart("closed_left")
    val closedWingRight = getPart("closed_right")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3(-0.5, 1.5, 0.0)
    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    //    lateinit var sleep: Pose
    lateinit var stand: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose

    override val cryAnimation = CryProvider { bedrockStateful("unfezant", "cry") }

    override fun registerPoses() {
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            animations = arrayOf(bedrock("unfezant", "sleep"))
//        )

        val blink = quirk { bedrockStateful("unfezant", "blink")}
        stand = registerPose(
            poseName = "stand",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER - PoseType.FLOAT + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = false),
                openWingRight.createTransformation().withVisibility(visibility = false),
                closedWingLeft.createTransformation().withVisibility(visibility = true),
                closedWingRight.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("unfezant", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY - PoseType.SWIM,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = false),
                openWingRight.createTransformation().withVisibility(visibility = false),
                closedWingLeft.createTransformation().withVisibility(visibility = true),
                closedWingRight.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("unfezant", "ground_idle"),
                BipedWalkAnimation(this)
            )
        )

        hover = registerPose(
            poseName = "floating",
            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = true),
                openWingRight.createTransformation().withVisibility(visibility = true),
                closedWingLeft.createTransformation().withVisibility(visibility = false),
                closedWingRight.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("unfezant", "air_idle"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = -10F.toRadians(), period = 0.9F, amplitude = 0.6F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Z_AXIS
                )
            )
        )

        fly = registerPose(
            poseName = "flying",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                openWingLeft.createTransformation().withVisibility(visibility = true),
                openWingRight.createTransformation().withVisibility(visibility = true),
                closedWingLeft.createTransformation().withVisibility(visibility = false),
                closedWingRight.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("unfezant", "air_idle"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = -14F.toRadians(), period = 0.9F, amplitude = 0.9F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Z_AXIS
                )
            )
        )
    }
}