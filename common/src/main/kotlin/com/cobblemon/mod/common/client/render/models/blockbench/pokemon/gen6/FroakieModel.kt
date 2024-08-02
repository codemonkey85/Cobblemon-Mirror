/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
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

class FroakieModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("froakie")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.2, -0.5, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var float: Pose
    lateinit var swim: Pose
    lateinit var walk: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("froakie", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("froakie", "blink")}
        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(bedrock("froakie", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES - PoseType.FLOAT,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("froakie", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseType = PoseType.WALK,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("froakie", "ground_walk")
                )
        )

        float = registerPose(
                poseName = "swim_idle",
                poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("froakie", "water_idle")
                )
        )

        swim = registerPose(
                poseName = "swim",
                poseTypes = setOf(PoseType.SWIM, PoseType.FLY),
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("froakie", "water_swim")
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
                        bedrock("froakie", "battle_idle")
                )

        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, battleidle, swim, float, sleep)) bedrockStateful("froakie", "faint") else null
}