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
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class RibombeeModel(root: ModelPart) : PokemonPosableModel(root){
    override val rootPart = root.registerChildWithAllChildren("ribombee")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(0.0, 0.95, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.1, 0.9, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose

    override fun registerPoses() {

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("ribombee", "sleep"))
        )

       standing = registerPose(
            poseName = "idle",
            poseTypes = STATIONARY_POSES + UI_POSES,
            animations = arrayOf(
                bedrock("ribombee", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            animations = arrayOf(
                bedrock("ribombee", "ground_walk")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("ribombee", "faint") else null
}