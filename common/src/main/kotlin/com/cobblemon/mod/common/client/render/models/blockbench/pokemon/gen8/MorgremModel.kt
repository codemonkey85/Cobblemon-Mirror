/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MorgremModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("morgrem")
    override val head = getPart("head")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 1.54F
    override var portraitTranslation = Vec3(0.1, 1.0, 0.0)

    override var profileScale = 0.69F
    override var profileTranslation = Vec3(0.04, 0.71, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("morgrem", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("morgrem", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("morgrem", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("morgrem", "ground_idle"),
                        BipedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F)
                )
        )
    }
}