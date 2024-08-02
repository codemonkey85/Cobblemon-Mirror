/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class LotadModel (root: ModelPart) : PokemonPosableModel(root), QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("lotad")

    override val foreLeftLeg = getPart("leg_left")
    override val foreRightLeg = getPart("leg_right")
    override val hindLeftLeg = getPart("leg_left2")
    override val hindRightLeg = getPart("leg_right2")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.15, -2.0, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.25, 0.0)

    lateinit var standing: Pose
    lateinit var waterstanding: Pose
    lateinit var walk: Pose
    lateinit var waterwalk: Pose
    lateinit var floating: Pose
    lateinit var swim: Pose
    lateinit var sleep: Pose

    val wateroffset = -2

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("charmander", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            quirks = arrayOf(blink),
            condition = { !it.isInWater },
            animations = arrayOf(
                bedrock("lotad", "ground_idle")
            )
        )

        waterstanding = registerPose(
            poseName = "waterstanding",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            animations = arrayOf(
                bedrock("lotad", "water_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            condition = { !it.isInWater },
            animations = arrayOf(
                bedrock("lotad", "ground_walk"),
            )
        )

        waterwalk = registerPose(
            poseName = "waterwalk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            animations = arrayOf(
                bedrock("lotad", "water_swim")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        floating = registerPose(
            poseName = "floating",
            quirks = arrayOf(blink),
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("lotad", "water_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        swim = registerPose(
            poseName = "swim",
            quirks = arrayOf(blink),
            poseType = PoseType.SWIM,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("lotad", "water_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        sleep = registerPose(
            poseName = "sleep",
            quirks = arrayOf(blink),
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("lotad", "sleep"),
            )
        )

    }
    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("lotad", "faint") else null
}