/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class YamaskModel (root: ModelPart) : PokemonPosableModel(root), BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("yamask")

    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.15, -0.45, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("yamask", "cry") }

    override fun registerPoses() {
        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("yamask", "sleep"))
        )

        val blink = quirk { bedrockStateful("yamask", "blink") }

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                    bedrock("yamask", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 5,
                quirks = arrayOf(blink),
                animations = arrayOf(
                    bedrock("yamask", "ground_walk")
                )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("yamask", "faint") else null
}