/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SkarmoryModel (root: ModelPart) : PokemonPosableModel(root), BipedFrame, BiWingedFrame, HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("skarmory")
    override val head = getPart("lower_neck")

    override val leftLeg = getPart("left_thigh")
    override val rightLeg = getPart("right_thigh")

    override val leftWing = getPart("left_wing")
    override val rightWing = getPart("right_wing")

    override var portraitScale = 2.49F
    override var portraitTranslation = Vec3(-1.05, 1.1, 0.0)

    override var profileScale = 0.69F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleeping: CobblemonPose
    lateinit var hovering: CobblemonPose
    lateinit var flying: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("skarmory", "blink") }

        sleeping = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("skarmory", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER + PoseType.UI_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("skarmory", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("skarmory", "ground_idle"),
                BipedWalkAnimation(this,0.6F, 1F)
            )
        )

        hovering = registerPose(
            poseName = "hovering",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("skarmory", "air_idle")
            )
        )

        flying = registerPose(
            poseName = "flying",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("skarmory", "air_idle")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("skarmory", "battle_idle")
            )
        )
    }
}