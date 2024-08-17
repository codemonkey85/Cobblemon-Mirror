/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ChanseyModel(root: ModelPart) : PokemonPosableModel(root), BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("chansey")
    override val leftLeg = getPart("left_foot")
    override val rightLeg = getPart("right_foot")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(-0.35, 0.3, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.55, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleIdle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("chansey", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("chansey", "blink") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("chansey", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            condition = { !it.isBattling },
            transformTicks = 10,
            animations = arrayOf(
                bedrock("chansey", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chansey", "ground_walk")
            )
        )

        battleIdle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            transformTicks = 10,
            animations = arrayOf(
                bedrock("chansey", "battle_idle")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("chansey", "faint")
}