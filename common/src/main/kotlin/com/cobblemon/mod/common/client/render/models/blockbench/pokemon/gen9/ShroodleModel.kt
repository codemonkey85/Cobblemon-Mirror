/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ShroodleModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("shroodle")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3d(0.0, -2.3, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3d(0.0, 0.4, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleeping: Pose

    override val cryAnimation = CryProvider { bedrockStateful("shroodle", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("shroodle", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("shroodle", "ground_idle")
            )
        )
        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("shroodle", "ground_walk")
            )
        )
        sleeping = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("shroodle", "sleep")
            )
        )
    }
}