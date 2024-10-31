/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BimanualSwingAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PoipoleModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BimanualFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("poipole")
    override val head = getPart("head")

    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.1, -0.66, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.6, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("poipole", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("poipole", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("poipole", "ground_idle"),
                BipedWalkAnimation(this, amplitudeMultiplier = 0.7F, periodMultiplier = 0.9F),
                BimanualSwingAnimation(this, amplitudeMultiplier = 0.8F, swingPeriodMultiplier = 0.9F)
                //bedrock("poipole", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("poipole", "faint") else null
}