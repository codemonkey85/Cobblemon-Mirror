/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BimanualSwingAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WingFlapIdleAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class HeracrossModel (root: ModelPart) : PokemonPosableModel(root), BipedFrame, BimanualFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("heracross")

    override val leftArm = getPart("arm_right")
    override val rightArm = getPart("arm_left")
    override val leftLeg = getPart("leg_right")
    override val rightLeg = getPart("leg_left")

    override val leftWing = getPart("wing_right")
    override val rightWing = getPart("wing_left")

    override var portraitScale = 1.6F
    override var portraitTranslation = Vec3(-0.2, 0.37, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(-0.02, 0.64, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var hovering: CobblemonPose
    lateinit var flying: CobblemonPose


    override fun registerPoses() {
        val blink = quirk { bedrockStateful("heracross", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.HOVER,
            quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        leftWing.createTransformation().withVisibility(visibility = false),
                        rightWing.createTransformation().withVisibility(visibility = false)
                ),
            animations = arrayOf(
                bedrock("heracross", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("heracross", "ground_idle"),
                BipedWalkAnimation(this,0.6F, 1F),
                BimanualSwingAnimation(this, 0.6F, 1F)
            )
        )

        hovering = registerPose(
            poseName = "hovering",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("heracross", "air_idle"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = 35F.toRadians(), period = 0.1F, amplitude = 0.6F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                )
            )
        )

        flying = registerPose(
                poseName = "flying",
                poseType = PoseType.FLY,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("heracross", "air_idle"),
                        WingFlapIdleAnimation(this,
                                flapFunction = sineFunction(verticalShift = 35F.toRadians(), period = 0.1F, amplitude = 0.6F),
                                timeVariable = { state, _, _ -> state.animationSeconds },
                                axis = ModelPartTransformation.Y_AXIS
                        )
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addRotationDegrees(ModelPartTransformation.X_AXIS, 45)

                )
        )
    }
}