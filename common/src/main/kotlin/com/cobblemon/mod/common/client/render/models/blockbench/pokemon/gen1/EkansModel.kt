/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveSegment
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class EkansModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {

    override val rootPart = root.registerChildWithAllChildren("ekans")
    private val body = getPart("body")
    override val head = getPart("head")
    private val tail = getPart("tail")
    private val tail2 = getPart("tail2")
    private val tail3 = getPart("tail3")
    private val tail4 = getPart("tail4")
    private val tail5 = getPart("tail5")
    private val tail6 = getPart("tail6")

    val tailSegment = WaveSegment(modelPart = tail, length = 9F)
    val tail2Segment = WaveSegment(modelPart = tail2, length = 9F)
    val tail3Segment = WaveSegment(modelPart = tail3, length = 9F)
    val tail4Segment = WaveSegment(modelPart = tail4, length = 9F)
    val tail5Segment = WaveSegment(modelPart = tail5, length = 10F)
    val tail6Segment = WaveSegment(modelPart = tail6, length = 10F)

    override var portraitScale = 1.7F
    override var portraitTranslation = Vec3(-0.3, -0.45, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(-0.05, 0.6, 0.0)

    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("ekans", "cry") }

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("ekans", "sleep"))
        )
        val blink = quirk { bedrockStateful("ekans", "blink") }
        registerPose(
            poseName = "normal",
            poseTypes = STATIONARY_POSES + MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ekans", "ground_idle"),
                WaveAnimation(
                    waveFunction = sineFunction(
                        period = 8F,
                        amplitude = 0.8F
                    ),
                    basedOnLimbSwing = true,
                    oscillationsScalar = 5F,
                    head = head,
                    rotationAxis = Y_AXIS,
                    motionAxis = X_AXIS,
                    moveHead = false,
                    headLength = 16F,
                    segments = arrayOf(
                        tailSegment,
                        tail2Segment,
                        tail3Segment,
                        tail4Segment,
                        tail5Segment,
                        tail6Segment
                    )
                )
            )
        )

        registerPose(
            poseName = "portrait",
            poseTypes = UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(bedrock("ekans", "summary_idle"))
        )
    }


    override fun getFaintAnimation(state: PosableState) = bedrockStateful("ekans", "faint")
}