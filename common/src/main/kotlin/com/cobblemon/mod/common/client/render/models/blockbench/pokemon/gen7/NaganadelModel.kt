/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class NaganadelModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("naganadel")
    override val head = getPart("head")

    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(0.0, 4.3, 0.0)

    override var profileScale = 0.4F
    override var profileTranslation = Vec3(0.05, 1.3, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("naganadel", "ground_idle"),
                wingFlap(
                    flapFunction = sineFunction(verticalShift = -10F.toRadians(), period = 0.9F, amplitude = 0.4F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = Y_AXIS
                ),
                rootPart.translation(function = sineFunction(amplitude = -3F, period = 0.9F), axis = Y_AXIS) { state, _, _ ->
                    state.animationSeconds
                }
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("naganadel", "ground_idle"),
                wingFlap(
                    flapFunction = sineFunction(verticalShift = -10F.toRadians(), period = 0.9F, amplitude = 0.4F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = Y_AXIS
                ),
                rootPart.translation(function = sineFunction(amplitude = -3F, period = 0.9F), axis = Y_AXIS) { state, _, _ -> state.animationSeconds }
                //bedrock("naganadel", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("naganadel", "faint") else null
}