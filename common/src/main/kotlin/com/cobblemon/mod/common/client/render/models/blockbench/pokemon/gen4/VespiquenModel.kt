/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class VespiquenModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("vespiquen")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-0.14, 0.8, 0.0)

    override var profileScale = 0.75F
    override var profileTranslation = Vec3(0.0, 0.6, 0.0)

    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose
    lateinit var standing: Pose

    override fun registerPoses() {
        val blink1 = quirk { bedrockStateful("vespiquen", "blink") }
        val wingsleep = quirk { bedrockStateful("vespiquen", "sleep_flap") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(wingsleep),
            animations = arrayOf(bedrock("vespiquen", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink1),
            animations = arrayOf(
                bedrock("vespiquen", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = PoseType.UI_POSES + PoseType.HOVER + PoseType.FLOAT,
            quirks = arrayOf(blink1),
            animations = arrayOf(
                bedrock("vespiquen", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM, PoseType.WALK),
            quirks = arrayOf(blink1),
            animations = arrayOf(
                bedrock("vespiquen", "air_fly")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(hover, fly, sleep, standing)) bedrockStateful("vespiquen", "faint") else null
}