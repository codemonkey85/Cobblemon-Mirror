/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GarganaclModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("garganacl")
    override val head = getPart("waist")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    val shoulder = getPart("shoulder_right")

    override var portraitScale = 2.6F
    override var portraitTranslation = Vec3(-0.4, 3.0, 0.0)

    override var profileScale = 0.45F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var battlestanding: Pose
    lateinit var walk: Pose
    lateinit var portrait: Pose

    override val cryAnimation = CryProvider { bedrockStateful("garganacl", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            condition = { !it.isBattling },
            poseTypes = STATIONARY_POSES + PoseType.PROFILE,
            transformedParts = arrayOf(
                shoulder.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("garganacl", "ground_idle")
            )
        )

        battlestanding = registerPose(
            poseName = "battlestanding",
            condition = { it.isBattling },
            poseTypes = STATIONARY_POSES,
            transformedParts = arrayOf(
                shoulder.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("garganacl", "battle_idle")
            )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
            transformedParts = arrayOf(
                shoulder.createTransformation().withVisibility(visibility = true)
            ),
                animations = arrayOf(bedrock("garganacl", "sleep"))
        )

        portrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            transformedParts = arrayOf(
                shoulder.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                bedrock("garganacl", "ground_idle")
            )
        )

        walk = registerPose(
                poseName = "walk",
                condition = { !it.isBattling },
                poseTypes = MOVING_POSES,
            transformedParts = arrayOf(
                shoulder.createTransformation().withVisibility(visibility = true)
            ),
                animations = arrayOf(
                    singleBoneLook(),
                    bedrock("garganacl", "ground_idle"),
                    bedrock("garganacl", "ground_walk")
                )
        )

    }
    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("garganacl", "faint") else null
}