/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.asExpressionLike
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class WoolooModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("wooloo")
    override val head = getPart("head")
    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")
    val wool = getPart("wool_shearable")

    override var portraitScale = 3.1F
    override var portraitTranslation = Vec3(-0.85, -1.8, 0.0)
    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("wooloo", "cry") }

    override fun registerPoses() {
        val isNotSheared = "q.has_aspect('sheared') == false".asExpressionLike()
        val blink = quirk { bedrockStateful("wooloo", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
            quirks = arrayOf(blink),
            transformedParts = arrayOf(wool.createTransformation().withVisibility(isNotSheared)),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wooloo", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
            quirks = arrayOf(blink),
            transformedParts = arrayOf(wool.createTransformation().withVisibility(visibility = isNotSheared)),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wooloo", "ground_walk")
            )
        )
    }

    override fun getEatAnimation(state: PosableState) = bedrockStateful("wooloo", "eat")

}