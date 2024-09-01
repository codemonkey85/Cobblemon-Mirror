/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CleffaModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("cleffa")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(0.0, -1.3, 0.0)

    override var profileScale = 1.15F
    override var profileTranslation = Vec3(0.0, 0.05, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var leftShoulder: Pose
    lateinit var rightShoulder: Pose

    override val cryAnimation = CryProvider { bedrockStateful("cleffa", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("cleffa", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("cleffa", "ground_idle")
            )
        )

        val shoulderDisplacement = 4.0

        leftShoulder = registerPose(
            poseName = "left_shoulder",
            poseType = PoseType.SHOULDER_LEFT,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("cleffa", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(X_AXIS, shoulderDisplacement)
            )
        )

        rightShoulder = registerPose(
            poseName = "right_shoulder",
            poseType = PoseType.SHOULDER_RIGHT,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("cleffa", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(X_AXIS, -shoulderDisplacement)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("cleffa", "ground_idle"),
                bedrock("cleffa", "ground_walk")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk)) bedrockStateful("cleffa", "faint") else null
}