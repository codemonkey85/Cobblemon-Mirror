/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class RoggenrolaModel (root: ModelPart) : PokemonPosableModel(root), BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("roggenrola")
    override val leftLeg = getPart("feet_left")
    override val rightLeg = getPart("feet_right")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.15, -1.75, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("roggenrola", "cry") }

    val shoulderOffset = 1

    override fun registerPoses() {
        val twitch = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("roggenrola", "quirk_twitch") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("roggenrola", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(twitch),
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("roggenrola", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(twitch),
            animations = arrayOf(
                bedrock("roggenrola", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(twitch),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("roggenrola", "battle_idle")
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            animations = arrayOf(
                bedrock("roggenrola", "shoulder_left")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, shoulderOffset)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            animations = arrayOf(
                bedrock("roggenrola", "shoulder_right")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -shoulderOffset)
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = bedrockStateful("roggenrola", "faint")
}