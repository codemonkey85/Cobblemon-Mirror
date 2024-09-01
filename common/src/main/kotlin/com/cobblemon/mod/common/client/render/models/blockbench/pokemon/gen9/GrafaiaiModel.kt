/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GrafaiaiModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("grafaiai")
    override val head = getPart("head")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(-0.1, 0.05, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleeping: Pose

    override val cryAnimation = CryProvider { bedrockStateful("grafaiai", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("grafaiai", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("grafaiai", "ground_idle")
            )
        )
        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("grafaiai", "ground_walk")
            )
        )
        sleeping = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("grafaiai", "sleep")
            )
        )
    }
}