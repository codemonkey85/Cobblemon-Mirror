/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SnubbullModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("snubbull")
    override val head = getPart("head")

    override val leftArm = getPart("left_arm")
    override val rightArm = getPart("right_arm")
    override val leftLeg = getPart("left_foot")
    override val rightLeg = getPart("right_foot")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.2, -0.3, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.76, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("snubbull", "cry") }

    override fun registerPoses() {

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("snubbull", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("snubbull", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("snubbull", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("snubbull", "battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("snubbull", "faint") else null
}