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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class RillaboomModel (root: ModelPart) : PosableModel(), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("rillaboom")
    override val head = getPart("head")

    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    val drums = getPart("drum")
    val stick_left = getPart("stick_left")
    val stick_right = getPart("stick_right")

    override val portraitScale = 2.3F
    override val portraitTranslation = Vec3d(-0.85, 1.65, 0.0)

    override val profileScale = 0.65F
    override val profileTranslation = Vec3d(0.0, 0.76, 0.0)

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
            condition = { (it.entity as? PokemonEntity)?.isBattling == false },
            transformedParts = arrayOf(
                drums.createTransformation().withVisibility(visibility = false),
                stick_left.createTransformation().withVisibility(visibility = true),
                stick_right.createTransformation().withVisibility(visibility = true)
            ),
            idleAnimations = arrayOf(
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
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("rillaboom", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { (it.entity as? PokemonEntity)?.isBattling == true },
            transformedParts = arrayOf(
                drums.createTransformation().withVisibility(visibility = true),
                stick_left.createTransformation().withVisibility(visibility = true),
                stick_right.createTransformation().withVisibility(visibility = true)
            ),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("rillaboom", "battle_idle")
            )

        )
    }
}