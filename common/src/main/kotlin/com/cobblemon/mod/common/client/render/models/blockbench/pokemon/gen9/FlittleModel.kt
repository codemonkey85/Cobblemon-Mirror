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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class FlittleModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("flittle")
    override val head = getPart("body")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(0.0, 0.0, 0.0)

    override var profileScale = 0.85F
    override var profileTranslation = Vec3(0.0, 0.65, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var hovering: Pose
    lateinit var flying: Pose
    lateinit var shoulderLeft: Pose
    lateinit var shoulderRight: Pose
    lateinit var sleep: Pose

    val shoulderOffsetX = 11
    val shoulderOffsetY = 6

    override val cryAnimation = CryProvider { bedrockStateful("flittle", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("flittle", "blink") }
        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("flittle", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            quirks = arrayOf(blink),
            poseTypes = UI_POSES + PoseType.STAND,
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("flittle", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            quirks = arrayOf(blink),
            poseType = PoseType.WALK,
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("flittle", "ground_walk")
            )
        )

        hovering = registerPose(
            poseName = "hovering",
            quirks = arrayOf(blink),
            poseTypes = setOf(PoseType.FLOAT,PoseType.HOVER),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("flittle", "air_idle")
            )
        )

        flying = registerPose(
            poseName = "flying",
            quirks = arrayOf(blink),
            poseTypes = setOf(PoseType.FLY,PoseType.SWIM),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("flittle", "air_fly")
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("flittle", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(shoulderOffsetX, shoulderOffsetY, 0.0)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("flittle", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(-shoulderOffsetX, shoulderOffsetY, 0.0)
            )
        )

    }
    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("flittle", "faint") else null
}