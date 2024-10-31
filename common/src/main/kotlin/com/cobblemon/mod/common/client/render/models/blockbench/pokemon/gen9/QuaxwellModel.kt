/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class QuaxwellModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("quaxwell")
    override val head = getPart("head")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.2, 1.8, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var floating: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("quaxwell", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("quaxwell", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("quaxwell", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.FLOAT,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxwell", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxwell", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseTypes = PoseType.SWIMMING_POSES,
                condition = { it.isInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxwell", "water_idle")
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
                bedrock("quaxwell", "battle_idle")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("quaxwell", "faint") else null
}