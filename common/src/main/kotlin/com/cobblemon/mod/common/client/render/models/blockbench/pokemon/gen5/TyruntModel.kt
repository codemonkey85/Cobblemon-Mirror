/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class TyruntModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("tyrunt")

    override val head = getPart("head")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitTranslation = Vec3d(-0.43, 0.40, 0.0)
    override var portraitScale = 1.08F

    override var profileTranslation = Vec3d(-0.03, 0.89, 0.0)
    override var profileScale = 0.56F

    //    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            idleAnimations = arrayOf(bedrock("tyrunt", "sleep"))
//        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("tyrunt", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("tyrunt", "ground_idle"),
                BipedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("tyrunt", "faint") else null
}