/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
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

class RillaboomModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("rillaboom")
    override val head = getPart("head")

    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    val drums = getPart("drum")
    val stick_left = getPart("stick_left")
    val stick_right = getPart("stick_right")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3(-0.85, 1.65, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.76, 0.0)

    lateinit var battleidle: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("rillaboom", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("rillaboom", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            transformedParts = arrayOf(
                drums.createTransformation().withVisibility(visibility = false),
                stick_left.createTransformation().withVisibility(visibility = true),
                stick_right.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rillaboom", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                drums.createTransformation().withVisibility(visibility = false),
                stick_left.createTransformation().withVisibility(visibility = true),
                stick_right.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rillaboom", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            transformedParts = arrayOf(
                drums.createTransformation().withVisibility(visibility = true),
                stick_left.createTransformation().withVisibility(visibility = true),
                stick_right.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rillaboom", "battle_idle")
            )

        )
    }
}