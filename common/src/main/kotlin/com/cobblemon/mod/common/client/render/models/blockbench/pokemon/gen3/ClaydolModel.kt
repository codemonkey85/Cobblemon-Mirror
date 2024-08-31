/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ClaydolModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("claydol")

    override var portraitScale = 1.6F
    override var portraitTranslation = Vec3(-0.7, 1.0, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("claydol", "sleep"))
        )

        val blink = quirk { bedrockStateful("claydol", "blink") }
        standing = registerPose(
            poseName = "hover",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("claydol", "air_idle")
            )
        )

        walk = registerPose(
            poseName = "fly",
            poseTypes = PoseType.MOVING_POSES + PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("claydol", "air_fly")
            )
        )
    }
}