/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CharizardModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("charizard")
    override val head = getPart("head_ai")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-0.5, 1.4, 0.0)

    override var profileScale = 0.55F
    override var profileTranslation = Vec3(0.05, 0.93, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var flyIdle: Pose
    lateinit var fly: Pose

    override fun registerPoses() {
        animations["physical"] = "q.bedrock_primary('charizard', 'physical', q.exclude_labels('look'), q.curve('symmetrical_wide'))".asExpressionLike()
        animations["special"] = "q.bedrock_primary('charizard', 'special', q.exclude_labels('look'), q.curve('symmetrical_wide'))".asExpressionLike()
        animations["status"] = "q.bedrock_primary('charizard', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["recoil"] = "q.bedrock_stateful('charizard', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('charizard', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('charizard', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("charizard", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("charizard", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("charizard", "ground_idle")
            )
        )

        registerPose(
            poseTypes = setOf(PoseType.STAND),
            poseName = "battle_standing",
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("charizard", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("charizard", "ground_idle"),
                bedrock("charizard", "ground_walk")
            )
        )

        flyIdle = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("charizard", "air_idle")
            ),
            transformedParts = arrayOf(rootPart.createTransformation().addPosition(Y_AXIS, -2F))
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("charizard", "air_fly")
            ),
            transformedParts = arrayOf(rootPart.createTransformation().addPosition(Y_AXIS, 6F))
        )
    }
}