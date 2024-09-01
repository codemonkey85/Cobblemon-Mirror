/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class WishiwashiSoloModel (root: ModelPart) : PokemonPosableModel(root){
    override val rootPart = root.registerChildWithAllChildren("wishiwashi_solo")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3(-0.4, -3.1, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.2, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose
    lateinit var watersleep: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("wishiwashi_solo", "blink") }
        watersleep = registerPose(
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            animations = arrayOf(bedrock("wishiwashi_solo", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES - PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wishiwashi_solo", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wishiwashi_solo", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wishiwashi_solo", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wishiwashi_solo", "water_swim")
            )
        )
    }
}