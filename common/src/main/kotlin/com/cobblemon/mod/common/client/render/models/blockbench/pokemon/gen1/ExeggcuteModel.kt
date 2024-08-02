/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ExeggcuteModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("exeggcute")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3(0.0, -1.9, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(-0.15, 0.0, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose
    lateinit var uiPortrait: Pose

    override fun registerPoses() {
        val blink1 = quirk { bedrockStateful("exeggcute", "blink") }
        val blink2 = quirk { bedrockStateful("exeggcute", "blink2") }
        val blink3 = quirk { bedrockStateful("exeggcute", "blink3") }
        val blink4 = quirk { bedrockStateful("exeggcute", "blink4") }
        val blink5 = quirk { bedrockStateful("exeggcute", "blink5") }
        val blink6 = quirk { bedrockStateful("exeggcute", "blink6") }
        uiPortrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            animations = arrayOf(
                bedrock("exeggcute", "portrait")
            )
        )

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("exeggcute", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + PoseType.PROFILE,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3, blink4, blink5, blink6),
            animations = arrayOf(
                bedrock("exeggcute", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("exeggcute", "ground_idle"),
                bedrock("exeggcute", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("exeggcute", "faint")
}