/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class OnixModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("onix")
    override val head = getPart("head")

    override var portraitScale = 1.1F
    override var portraitTranslation = Vec3(-0.2, 1.4, 0.0)

    override var profileScale = 0.55F
    override var profileTranslation = Vec3(-0.1, 0.9, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var ui: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("onix", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("onix", "blink") }
        ui = registerPose(
            poseName = "ui",
            poseTypes = UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("onix", "summary_idle")
            )
        )

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 0,
            animations = arrayOf(
                bedrock("onix", "sleep"),
                bedrock("onix", "slow_boulder_rotation")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES,
            transformTicks = 1,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("onix", "ground_idle"),
                bedrock("onix", "boulder_rotation")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 1,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("onix", "ground_walk"),
                bedrock("onix", "boulder_rotation")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("onix", "battle_idle"),
                bedrock("onix", "boulder_rotation")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("onix", "faint")
}