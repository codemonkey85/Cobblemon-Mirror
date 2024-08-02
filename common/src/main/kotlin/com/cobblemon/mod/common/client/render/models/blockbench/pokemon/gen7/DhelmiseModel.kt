/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DhelmiseModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("dhelmise")

    override var portraitScale = 1.1F
    override var portraitTranslation = Vec3(-0.5, 2.7, 0.0)

    override var profileScale = 0.35F
    override var profileTranslation = Vec3(0.0, 1.35, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformTicks = 10,
                animations = arrayOf(bedrock("dhelmise", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("dhelmise", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("dhelmise", "ground_walk")
                )
        )

        float = registerPose(
                poseName = "float",
                poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
                animations = arrayOf(
                        bedrock("dhelmise", "water_idle")
                )
        )

        swim = registerPose(
                poseName = "swim",
                poseType = PoseType.SWIM,
                animations = arrayOf(
                        bedrock("dhelmise", "water_swim")
                )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("dhelmise", "faint") else null
}