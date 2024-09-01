/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BimanualSwingAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MawileModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("mawile")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")

    override var portraitScale = 2.93F
    override var portraitTranslation = Vec3(0.2, -1.06, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.09, 0.6, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("mawile", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
                condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mawile", "ground_idle")
            )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling },
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("mawile", "battle_idle")
                )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 5,
            quirks = arrayOf(blink),
            animations = arrayOf(
                    singleBoneLook(),
                    bedrock("mawile", "ground_idle"),
                    BipedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F),
                    BimanualSwingAnimation(this, swingPeriodMultiplier = 0.6F, amplitudeMultiplier = 0.9F)
            )
        )
    }
}