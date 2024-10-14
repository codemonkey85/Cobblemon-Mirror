/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveSegment
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.FLYING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STANDING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.SWIMMING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GyaradosModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("gyarados")

    val seg1 = getPart("segment1")
    val seg2 = getPart("segment2")
    val seg3 = getPart("segment3")
    val seg4 = getPart("segment4")
    val seg5 = getPart("segment5")
    val seg6 = getPart("segment6")
    val seg7 = getPart("segment7")
    val seg8 = getPart("segment8")
    val seg9 = getPart("segment9")
    val seg10 = getPart("segment10")
    val seg11 = getPart("segment11")
    val seg12 = getPart("segment12")

    override val head = getPart("head")

    val wseg1 = WaveSegment(seg1, 7F)
    val wseg2 = WaveSegment(seg2, 5F)
    val wseg3 = WaveSegment(seg3, 6F)
    val wseg4 = WaveSegment(seg4, 6F)
    val wseg5 = WaveSegment(seg5, 6F)
    val wseg6 = WaveSegment(seg6, 6F)
    val wseg7 = WaveSegment(seg7, 6F)
    val wseg8 = WaveSegment(seg8, 6F)
    val wseg9 = WaveSegment(seg9, 6F)
    val wseg10 = WaveSegment(seg10, 5F)
    val wseg11 = WaveSegment(seg11, 5F)
    val wseg12 = WaveSegment(seg12, 4F)

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-1.55, 0.35, 0.0)
    override var profileScale = 0.7F
    override var profileTranslation = Vec3(-0.1, 0.65, 0.0)

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("gyarados", "blink")}
        registerPose(
            poseName = "land",
            poseTypes = STANDING_POSES + UI_POSES,
            quirks = arrayOf(blink),
            transformTicks = 20,
            condition = { !it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gyarados", "ground_idle"),
                WaveAnimation(
                    waveFunction = sineFunction(
                        period = 8F,
                        amplitude = 0.4F
                    ),
                    basedOnLimbSwing = true,
                    oscillationsScalar = 8F,
                    head = seg5,
                    rotationAxis = Y_AXIS,
                    motionAxis = X_AXIS,
                    headLength = 0.1F,
                    segments = arrayOf(
                        wseg6,
                        wseg7,
                        wseg8,
                        wseg9,
                        wseg10,
                        wseg11,
                        wseg12
                    )
                )
            )
        )

        registerPose(
            poseName = "surface",
            poseTypes = setOf(PoseType.STAND, PoseType.WALK),
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            transformTicks = 20,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gyarados", "surface_idle"),
                WaveAnimation(
                    waveFunction = sineFunction(
                        period = 3F,
                        amplitude = 0.2F
                    ),
                    oscillationsScalar = 24F,
                    head = seg6,
                    rotationAxis = X_AXIS,
                    motionAxis = Y_AXIS,
                    headLength = 0F,
                    basedOnLimbSwing = false,
                    moveHead = false,
                    segments = arrayOf(
                        wseg7,
                        wseg8,
                        wseg9,
                        wseg10,
                        wseg11,
                        wseg12
                    )
                )
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(Y_AXIS, -6F)
            )
        )

        registerPose(
            poseName = "swim",
            poseTypes = SWIMMING_POSES + FLYING_POSES,
//            transformedParts = arrayOf(head.withRotation(X_AXIS, -70F.toRadians())),
            quirks = arrayOf(blink),
            transformTicks = 20,
            animations = arrayOf(
                singleBoneLook(),
                WaveAnimation(
                    waveFunction = sineFunction(
                        period = 3F,
                        amplitude = 0.4F
                    ),
                    oscillationsScalar = 24F,
                    head = rootPart,
                    rotationAxis = X_AXIS,
                    motionAxis = Y_AXIS,
                    headLength = 4F,
                    moveHead = true,
                    segments = arrayOf(
                        wseg1,
                        wseg2,
                        wseg3,
                        wseg4,
                        wseg5,
                        wseg6,
                        wseg7,
                        wseg8,
                        wseg9,
                        wseg10,
                        wseg11,
                        wseg12
                    )
                )
            )
        )
    }
}