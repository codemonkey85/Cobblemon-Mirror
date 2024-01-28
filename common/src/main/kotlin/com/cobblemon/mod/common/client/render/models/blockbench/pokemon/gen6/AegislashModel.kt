/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class AegislashModel(root: ModelPart) : PosableModel() {
    override val rootPart = root.registerChildWithAllChildren("aegislash")

    override val portraitScale = 3.0F
    override val portraitTranslation = Vec3d(-0.2, 1.3, 0.0)

    override val profileScale = 0.65F
    override val profileTranslation = Vec3d(0.0, 0.85, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("aegislash", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("aegislash", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                idleAnimations = arrayOf(
                        bedrock("aegislash", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                idleAnimations = arrayOf(
                        bedrock("aegislash", "ground_walk")
                )
        )
    }
}