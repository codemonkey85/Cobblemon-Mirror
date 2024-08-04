/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class WartortleModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("wartortle")
    override val head = getPart("head_ai")

    override var portraitScale = 1.57F
    override var portraitTranslation = Vec3(-0.05, 0.54, 0.0)

    override var profileScale = 0.69F
    override var profileTranslation = Vec3(-0.04, 0.69, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose
    lateinit var floating: CobblemonPose
    lateinit var swimming: CobblemonPose
    lateinit var water_surface_idle: CobblemonPose
    lateinit var water_surface_swim: CobblemonPose

    val shoulderOffset = 5.5
    val wateroffset = -10

    override fun registerPoses() {
        animations["physical"] = "q.bedrock_primary('wartortle', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["special"] = "q.bedrock_primary('wartortle', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["status"] = "q.bedrock_primary('wartortle', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["recoil"] = "q.bedrock_stateful('wartortle', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('wartortle', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('wartortle', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("wartortle", "blink") }
        val quirkidle = quirk { bedrockStateful("wartortle", "quirk_idle") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("wartortle", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER + UI_POSES,
            quirks = arrayOf(blink, quirkidle),
            condition = { !it.isBattling && !it.isInWater && !it.isUnderWater},
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "ground_idle")
            )
        )

        battleidle = registerPose(
            poseTypes = setOf(PoseType.STAND),
            poseName = "battle_standing",
            quirks = arrayOf(blink, quirkidle),
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            condition = { !it.isInWater && !it.isUnderWater},
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            condition = { it.isUnderWater },
            namedAnimations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
            condition = { it.isUnderWater },
            namedAnimations = mutableMapOf("faint" to faint),
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "water_swim"),
            )
        )

        water_surface_idle = registerPose(
            poseName = "surface_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            namedAnimations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink),
            condition = { !it.isUnderWater && it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "surfacewater_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        water_surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isUnderWater && it.isInWater },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "surfacewater_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )
    }
}