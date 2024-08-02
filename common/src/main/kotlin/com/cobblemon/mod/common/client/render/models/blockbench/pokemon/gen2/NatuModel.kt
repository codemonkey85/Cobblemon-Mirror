/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class NatuModel(root: ModelPart) : PokemonPosableModel(root), BipedFrame, HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("natu")
    override val head = getPart("torso")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.33F
    override var portraitTranslation = Vec3(0.04, -1.66, 0.0)

    override var profileScale = 1.09F
    override var profileTranslation = Vec3(0.0, 0.03, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    override fun registerPoses() {
        val blink = quirk { bedrockStateful("natu", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("natu", "sleep")
            )
        )
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F, yawMultiplier = 0F),
                bedrock("natu", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F, yawMultiplier = 0F),
                bedrock("natu", "ground_idle"),
                BipedWalkAnimation(this, periodMultiplier = 0.8F, amplitudeMultiplier = 0.6F)
//                bedrock("natu", "ground_walk")
            )
        )
    }
}