/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.triangleFunction
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class VibravaModel  (root: ModelPart) : PokemonPosableModel(root), QuadrupedFrame, HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("vibrava")
    override val head = getPart("head")

    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 1.36F
    override var portraitTranslation = Vec3(-0.37, -0.55, 0.0)

    override var profileScale = 0.54F
    override var profileTranslation = Vec3(-0.01, 0.71, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("vibrava", "cry") }

    val wing_front_left = getPart("wing_front_left")
    val wing_front_right = getPart("wing_front_right")

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("vibrava", "blink") }

        val wingFrame1 = object : BiWingedFrame {
            override val rootPart = this@VibravaModel.rootPart
            override val leftWing = getPart("wing_front_left")
            override val rightWing = getPart("wing_front_right")
        }

        val wingFrame2 = object : BiWingedFrame {
            override val rootPart = this@VibravaModel.rootPart
            override val leftWing = getPart("wing_back_left")
            override val rightWing = getPart("wing_back_right")
        }

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.HOVER,
                transformTicks = 30,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(pitchMultiplier = 0.6F, yawMultiplier = 0.3F),
                        bedrock("vibrava", "ground_idle")
                ),
                transformedParts = arrayOf(
                        wing_front_left.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, -75),
                        wing_front_right.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, 75)
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES + PoseType.HOVER,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(pitchMultiplier = 0.6F, yawMultiplier = 0.3F),
                        bedrock("vibrava", "ground_idle"),
                        wingFrame1.wingFlap(
                                flapFunction = triangleFunction( period = 0.08F, amplitude = 0.6F),
                                timeVariable = { state, _, _ -> state.animationSeconds },
                                axis = ModelPartTransformation.Z_AXIS
                        ),
                        wingFrame2.wingFlap(
                                flapFunction = triangleFunction( period = 0.1F, amplitude = 0.4F),
                                timeVariable = { state, _, _ -> 0.01F + state.animationSeconds },
                                axis = ModelPartTransformation.Z_AXIS
                        )
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -4),
                        wing_front_left.createTransformation().addRotationDegrees(ModelPartTransformation.Z_AXIS, -30),
                        wing_front_right.createTransformation().addRotationDegrees(ModelPartTransformation.Z_AXIS, 30)

                )
        )
    }
}