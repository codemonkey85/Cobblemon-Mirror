/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
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
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PidgeottoModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("pidgeotto")
    override val head = getPart("neck")

    override val leftWing = getPart("wing_closed_left")
    override val rightWing = getPart("wing_closed_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    private val tail = getPart("tail")

    private val wingOpenRight = getPart("wing_open_right")
    private val wingOpenLeft = getPart("wing_open_left")
    private val wingClosedRight = getPart("wing_closed_right")
    private val wingClosedLeft = getPart("wing_closed_left")

    override var portraitScale = 2.8F
    override var portraitTranslation = Vec3(-0.4, -0.9, 0.0)
    override var profileScale = 1.1F
    override var profileTranslation = Vec3(0.0, 0.1, 0.0)

    lateinit var sleep: Pose
    lateinit var stand: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose

    override val cryAnimation = CryProvider { bedrockStateful("pidgeotto", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("pidgeotto", "blink")}
        val flyQuirk1 = quirk { bedrockStateful("pidgeotto", "air_fly_quirk") }
        val flyQuirk2 = quirk { bedrockStateful("pidgeotto", "air_fly_quirk2") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = true),
                wingClosedRight.createTransformation().withVisibility(visibility = true),
                wingOpenLeft.createTransformation().withVisibility(visibility = false),
                wingOpenRight.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(bedrock("pidgeotto", "sleep_PLACEHOLDER"))
        )

        stand = registerPose(
            poseName = "stand",
            poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES,
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = true),
                wingClosedRight.createTransformation().withVisibility(visibility = true),
                wingOpenLeft.createTransformation().withVisibility(visibility = false),
                wingOpenRight.createTransformation().withVisibility(visibility = false)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeotto", "ground_idle_PLACEHOLDER")
            )
        )
        walk = registerPose(
            poseName = "hover",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = true),
                wingClosedRight.createTransformation().withVisibility(visibility = true),
                wingOpenLeft.createTransformation().withVisibility(visibility = false),
                wingOpenRight.createTransformation().withVisibility(visibility = false)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeotto", "ground_idle_PLACEHOLDER"),
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
        hover = registerPose(
            poseName = "fly",
            poseType = PoseType.HOVER,
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = false),
                wingClosedRight.createTransformation().withVisibility(visibility = false),
                wingOpenLeft.createTransformation().withVisibility(visibility = true),
                wingOpenRight.createTransformation().withVisibility(visibility = true)
            ),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeotto", "air_idle")
            )
        )
        fly = registerPose(
            poseName = "walk",
            poseType = PoseType.FLY,
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = false),
                wingClosedRight.createTransformation().withVisibility(visibility = false),
                wingOpenLeft.createTransformation().withVisibility(visibility = true),
                wingOpenRight.createTransformation().withVisibility(visibility = true)
            ),
            transformTicks = 10,
            quirks = arrayOf(blink, flyQuirk1, flyQuirk2),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeotto", "air_fly")
            )
        )
    }
}