/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ZigzagoonModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("zigzagoon")
    override val head = getPart("head")

    override var portraitScale = 2.4F
    override var portraitTranslation = Vec3(-0.4, -2.2, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.2, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose
    lateinit var shoulderLeft: Pose
    lateinit var shoulderRight: Pose

    val shoulderOffsetX = 0
    val shoulderOffsetY = 0
    val shoulderOffsetZ = -0.5

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("zigzagoon", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("zigzagoon", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zigzagoon", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zigzagoon", "ground_walk")
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
                bedrock("zigzagoon", "battle_idle")
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zigzagoon", "shoulder_left")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(shoulderOffsetX, shoulderOffsetY, shoulderOffsetZ)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zigzagoon", "shoulder_right")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(-shoulderOffsetX, shoulderOffsetY, shoulderOffsetZ)
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("zigzagoon", "faint") else null
}