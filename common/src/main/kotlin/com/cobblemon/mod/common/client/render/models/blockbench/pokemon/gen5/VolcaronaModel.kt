/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class VolcaronaModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("volcarona")
    override val head = getPart("head")

    val rightwings = getPart("wings_right")
    val leftwings = getPart("wings_left")
    val rightfluff = getPart("wing_base_right")
    val leftfluff = getPart("wing_base_left")

    override var portraitScale = 1.83F
    override var portraitTranslation = Vec3(-0.62, 1.89, 0.0)

    override var profileScale = 0.46F
    override var profileTranslation = Vec3(0.0, 1.06, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("volcarona", "cry") }

    override fun registerPoses() {

        val wingFrame = object : BiWingedFrame {
            override val rootPart = this@VolcaronaModel.rootPart
            override val leftWing = getPart("wings_left")
            override val rightWing = getPart("wings_right")
        }

        val fluffFrame = object : BiWingedFrame {
            override val rootPart = this@VolcaronaModel.rootPart
            override val leftWing = getPart("wing_base_left")
            override val rightWing = getPart("wing_base_right")
        }

        standing = registerPose(
            poseName = "standing",
            transformTicks = 20,
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
            animations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.2F, yawMultiplier = 0.3F),
                bedrock("volcarona", "ground_idle"),
                wingFrame.wingFlap(
                    flapFunction = sineFunction( period = 1F, amplitude = 0.25F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                ),
                fluffFrame.wingFlap(
                    flapFunction = sineFunction( period = 1F, amplitude = 0.3F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                )
            ),
            transformedParts = arrayOf(
                rightwings.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, 5),
                leftwings.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, -5),
                rightfluff.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, 17),
                leftfluff.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, -17)
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformTicks = 20,
            poseTypes = PoseType.MOVING_POSES,
            animations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.2F, yawMultiplier = 0.3F),
                bedrock("volcarona", "ground_idle"),
                wingFrame.wingFlap(
                    flapFunction = sineFunction( period = 0.8F, amplitude = 0.25F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                ),
                fluffFrame.wingFlap(
                    flapFunction = sineFunction( period = 0.8F, amplitude = 0.3F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Y_AXIS
                )
                //bedrock("volcarona", "ground_walk")
            ),
            transformedParts = arrayOf(
                rightwings.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, 5),
                leftwings.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, -5),
                rightfluff.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, 17),
                leftfluff.createTransformation().addRotationDegrees(ModelPartTransformation.Y_AXIS, -17),
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -5)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("volcarona", "faint") else null
}