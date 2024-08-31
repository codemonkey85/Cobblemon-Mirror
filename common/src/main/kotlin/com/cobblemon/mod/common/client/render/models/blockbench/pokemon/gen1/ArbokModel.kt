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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ArbokModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("arbok")
    override val head = getPart("head_ai")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(-0.4, 1.0, 0.0)

    override var profileScale = 0.67F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var summary: Pose

    val tail = getPart("tail")
    val tail2 = getPart("tail2")
    val tail3 = getPart("tail3")
    val tail4 = getPart("tail4")
    val tail5 = getPart("tail5")
    val tailWaveSegment = WaveSegment(modelPart = tail, length = 11F)
    val tail2WaveSegment = WaveSegment(modelPart = tail2, length = 11F)
    val tail3WaveSegment = WaveSegment(modelPart = tail3, length = 11F)
    val tail4WaveSegment = WaveSegment(modelPart = tail4, length = 11F)
    val tail5WaveSegment = WaveSegment(modelPart = tail5, length = 11F)

    override val cryAnimation = CryProvider { bedrockStateful("arbok", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("arbok", "blink") }
        // TODO tongue_flick

        val wave = WaveAnimation(
            waveFunction = sineFunction(
                period = 10F,
                amplitude = 0.5F
            ),
            basedOnLimbSwing = true,
            oscillationsScalar = 8F,
            head = tail,
            rotationAxis = ModelPartTransformation.Y_AXIS,
            motionAxis = ModelPartTransformation.X_AXIS,
            headLength = 0.1F,
            segments = arrayOf(
                tailWaveSegment,
                tail2WaveSegment,
                tail3WaveSegment,
                tail4WaveSegment,
                tail5WaveSegment
            )
        )

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("arbok", "sleep"))
        )


        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arbok", "summary_idle"),
                wave
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arbok", "ground_walk"),
                wave
            )
        )

        summary = registerPose(
            poseName = "summary",
            poseTypes = UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arbok", "summary_idle")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("arbok", "faint") else null
}