/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class QuilladinModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("quilladin")
    override val head = getPart("torso")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2F
    override var portraitTranslation = Vec3(-0.3, 0.1, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("quilladin", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("quilladin", "blink")}

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quilladin", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quilladin", "ground_walk")
            )
        )

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            animations = arrayOf(bedrock("quilladin", "sleep"))
        )
    }
}