/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.parabolaFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class NoctowlModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("noctowl")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val head = getPart("head")
    private val tail = getPart("tail")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.35, 1.0, 0.0)
    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.68, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose

    override val cryAnimation = CryProvider { bedrockStateful("noctowl", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("noctowl", "blink")}
        standing = registerPose(
            poseName = "stand",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER - PoseType.FLOAT + PoseType.UI_POSES,
            transformTicks = 0,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("noctowl", "ground_idle")
            )
        )
        hover = registerPose(
            poseName = "hover",
            poseTypes = setOf(PoseType.HOVER, PoseType.FLOAT),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("noctowl", "ground_idle")
            )
        )
        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("noctowl", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY - PoseType.SWIM,
            transformTicks = 5,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("noctowl", "ground_idle"),
                rootPart.translation(
                    function = parabolaFunction(
                        peak = -4F,
                        period = 0.4F
                    ),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                ),
                head.translation(
                    function = sineFunction(
                        amplitude = (-20F).toRadians(),
                        period = 1F,
                        verticalShift = (-10F).toRadians()
                    ),
                    axis = ModelPartTransformation.X_AXIS,
                    timeVariable = { state, _, _ -> state.animationSeconds }
                ),
                leftLeg.rotation(
                    function = parabolaFunction(
                        tightness = -20F,
                        phaseShift = 0F,
                        verticalShift = (30F).toRadians()
                    ),
                    axis = ModelPartTransformation.X_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                rightLeg.rotation(
                    function = parabolaFunction(
                        tightness = -20F,
                        phaseShift = 0F,
                        verticalShift = (30F).toRadians()
                    ),
                    axis = ModelPartTransformation.X_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                tail.rotation(
                    function = sineFunction(
                        amplitude = (-5F).toRadians(),
                        period = 1F
                    ),
                    axis = ModelPartTransformation.X_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                wingFlap(
                    flapFunction = sineFunction(
                        amplitude = (-5F).toRadians(),
                        period = 0.4F,
                        phaseShift = 0.00F,
                        verticalShift = (-20F).toRadians()
                    ),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Z_AXIS
                ),
                rightWing.translation(
                    function = parabolaFunction(
                        tightness = -10F,
                        phaseShift = 30F,
                        verticalShift = (25F).toRadians()
                    ),
                    axis = ModelPartTransformation.Y_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                leftWing.translation(
                    function = parabolaFunction(
                        tightness = -10F,
                        phaseShift = 30F,
                        verticalShift = (25F).toRadians()
                    ),
                    axis = ModelPartTransformation.Y_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
            )
        )
    }
}