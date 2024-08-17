/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WingFlapIdleAnimation
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

class ScytherModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("scyther")
    override val head = getPart("head")
    override val leftLeg = getPart("left_upper_leg")
    override val rightLeg = getPart("right_upper_leg")
    override val leftWing = getPart("left_wing")
    override val rightWing = getPart("right_wing")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3(-0.4, 1.4, 0.0)

    override var profileScale = 0.64F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var hover: CobblemonPose
    lateinit var fly: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("scyther", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("scyther", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES - PoseType.HOVER,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("scyther", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                BipedWalkAnimation(this, periodMultiplier = 0.7F, amplitudeMultiplier = 0.85F),
                bedrock("scyther", "ground_idle")
                //bedrock("scyther", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("scyther", "air_idle"),
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
            transformTicks = 10,
            animations = arrayOf(
                bedrock("scyther", "air_idle"),
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
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("scyther", "faint") else null
}