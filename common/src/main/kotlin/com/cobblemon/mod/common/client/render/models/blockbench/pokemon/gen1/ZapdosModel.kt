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
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ZapdosModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BiWingedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("zapdos")
    override val head = getPart("head")
    override val leftLeg = getPart("leftleg")
    override val rightLeg = getPart("rightleg")
    override val leftWing = getPart("left_wing")
    override val rightWing = getPart("right_wing")

    override var portraitScale = 2.7F
    override var portraitTranslation = Vec3(-0.9, -0.65, 0.0)

    override var profileScale = 0.85F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + UI_POSES - PoseType.HOVER,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zapdos", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            animations = arrayOf(
                singleBoneLook(),
                BipedWalkAnimation(this, periodMultiplier = 0.7F, amplitudeMultiplier = 0.85F),
                bedrock("zapdos", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = setOf(PoseType.HOVER, PoseType.FLOAT),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zapdos", "air_idle"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = -10F.toRadians(), period = 0.9F, amplitude = 0.6F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                )
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zapdos", "air_fly"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = -14F.toRadians(), period = 0.9F, amplitude = 0.6F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                )
            )
        )
    }


//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("zapdos", "faint") else null
}