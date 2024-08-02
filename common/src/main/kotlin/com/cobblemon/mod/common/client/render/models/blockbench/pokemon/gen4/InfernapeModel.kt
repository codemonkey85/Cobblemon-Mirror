/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class InfernapeModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("infernape")
    override val head = getPart("head_ai")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.65, 1.55, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("infernape", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("infernape", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = STATIONARY_POSES + UI_POSES,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("infernape", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = MOVING_POSES,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("infernape", "ground_walk")
                )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk)) bedrockStateful("infernape", "faint") else null
}