/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class BrionneModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("brionne")
    override val head = getPart("head")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3(-0.4, -0.3, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var float: Pose
    lateinit var swim: Pose
    lateinit var battleidle: Pose
//    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("brionne", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("brionne", "blink") }
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            idleAnimations = arrayOf(bedrock("brionne", "sleep"))
//        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("brionne", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("brionne", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                    singleBoneLook(),
                    bedrock("brionne", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                    singleBoneLook(),
                    bedrock("brionne", "water_swim")
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
                bedrock("brionne", "battle_idle")
            )

        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, battleidle, swim, float)) bedrockStateful("brionne", "faint") else null
}