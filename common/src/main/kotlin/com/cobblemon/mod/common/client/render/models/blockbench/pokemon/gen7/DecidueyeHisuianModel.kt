/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WingFlapIdleAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DecidueyeHisuianModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("decidueye_hisui")
    override val head = getPart("head")

    private val leftClosedWing = getPart("wing_closed_left1")
    private val rightClosedWing = getPart("wing_closed_right1")
    override val leftWing = getPart("wing_open_left1")
    override val rightWing = getPart("wing_open_right1")

    override val leftLeg = getPart("thigh_left")
    override val rightLeg = getPart("thigh_right")

    val arrow = getPart("arrow")

    override var portraitTranslation = Vec3(-0.28, 2.5300000000000047, 0.0)
    override var portraitScale = 1.5200002F

    override var profileTranslation = Vec3(0.0, 1.0299999999999998, 0.0)
    override var profileScale = 0.46999997F

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var hover: CobblemonPose
    lateinit var fly: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("decidueye", "hisuian_cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("decidueye", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES,
                transformedParts = arrayOf(
                        leftClosedWing.createTransformation().withVisibility(visibility = true),
                        rightClosedWing.createTransformation().withVisibility(visibility = true),
                        leftWing.createTransformation().withVisibility(visibility = false),
                        rightWing.createTransformation().withVisibility(visibility = false),
                        arrow.createTransformation().withVisibility(visibility = false)
                ),
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("decidueye", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = MOVING_POSES - PoseType.FLY,
                transformedParts = arrayOf(
                        leftClosedWing.createTransformation().withVisibility(visibility = true),
                        rightClosedWing.createTransformation().withVisibility(visibility = true),
                        leftWing.createTransformation().withVisibility(visibility = false),
                        rightWing.createTransformation().withVisibility(visibility = false),
                        arrow.createTransformation().withVisibility(visibility = false)
                ),
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        BipedWalkAnimation(this),
                        bedrock("decidueye", "ground_idle")
                )
        )

        hover = registerPose(
                poseName = "hover",
                poseType = PoseType.HOVER,
                transformedParts = arrayOf(
                        leftClosedWing.createTransformation().withVisibility(visibility = false),
                        rightClosedWing.createTransformation().withVisibility(visibility = false),
                        leftWing.createTransformation().withVisibility(visibility = true),
                        rightWing.createTransformation().withVisibility(visibility = true),
                        arrow.createTransformation().withVisibility(visibility = false)
                ),
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("decidueye", "air_idle"),
                        WingFlapIdleAnimation(this,
                                flapFunction = sineFunction(verticalShift = -10F.toRadians(), period = 0.9F, amplitude = 0.6F),
                                timeVariable = { state, _, _ -> state.animationSeconds },
                                axis = ModelPartTransformation.Y_AXIS
                        )
                )
        )

        fly = registerPose(
                poseName = "fly",
                poseType = PoseType.FLY,
                transformedParts = arrayOf(
                        leftClosedWing.createTransformation().withVisibility(visibility = false),
                        rightClosedWing.createTransformation().withVisibility(visibility = false),
                        leftWing.createTransformation().withVisibility(visibility = true),
                        rightWing.createTransformation().withVisibility(visibility = true),
                        arrow.createTransformation().withVisibility(visibility = false)
                ),
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("decidueye", "air_fly"),
                        WingFlapIdleAnimation(this,
                                flapFunction = sineFunction(verticalShift = -14F.toRadians(), period = 0.9F, amplitude = 0.9F),
                                timeVariable = { state, _, _ -> state.animationSeconds },
                                axis = ModelPartTransformation.Y_AXIS
                        )
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("decidueye", "faint") else null
}