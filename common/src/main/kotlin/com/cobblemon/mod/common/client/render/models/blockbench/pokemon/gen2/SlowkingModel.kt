/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SlowkingModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("slowking")
    override val head = getPart("head")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.1, 0.1, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.55, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var float: Pose
    lateinit var swim: Pose
    lateinit var battleidle: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("slowking", "blink") }
        val blink2 = quirk { bedrockStateful("slowking", "blink2") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            quirks = arrayOf(blink, blink2),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("slowking", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink, blink2),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("slowking", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            quirks = arrayOf(blink, blink2),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("slowking", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink, blink2),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("slowking", "water_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, blink2),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("slowking", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("slowking", "faint") else null
}