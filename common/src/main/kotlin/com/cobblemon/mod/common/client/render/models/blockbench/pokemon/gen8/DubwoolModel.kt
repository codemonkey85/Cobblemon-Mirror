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
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.asExpressionLike
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DubwoolModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("dubwool")
    override val head = getPart("head")
    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")
    val wool = getPart("wool_shearable")
    val neckWool = getPart("neck_wool_shearable")

    override var portraitScale = 3.1F
    override var portraitTranslation = Vec3(-1.2, -0.7, 0.0)
    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("dubwool", "cry") }

    override fun registerPoses() {
        val isNotSheared = "q.has_aspect('${DataKeys.HAS_BEEN_SHEARED}') == false".asExpressionLike()
        val blink = quirk { bedrockStateful("dubwool", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            transformedParts = arrayOf(
                wool.createTransformation().withVisibility(visibility = isNotSheared),
                neckWool.createTransformation().withVisibility(visibility = isNotSheared)
            ),
            animations = arrayOf(bedrock("dubwool", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
            transformTicks = 10,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                wool.createTransformation().withVisibility(visibility = isNotSheared),
                neckWool.createTransformation().withVisibility(visibility = isNotSheared)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dubwool", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
            transformTicks = 10,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                wool.createTransformation().withVisibility(visibility = isNotSheared),
                neckWool.createTransformation().withVisibility(visibility = isNotSheared)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dubwool", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("dubwool", "faint") else null
    override fun getEatAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("dubwool", "eat") else null
}