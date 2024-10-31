/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WingFlapIdleAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DartrixModel(root: ModelPart) : PokemonPosableModel(root), BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("dartrix")

    private val wingsOpen = getPart("wings_open")
    private val wingsClosed = getPart("wings_closed")

    override val leftWing = getPart("wing_left_open")
    override val rightWing = getPart("wing_right_open")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitTranslation = Vec3(-0.23, 1.14, 0.0)
    override var portraitScale = 1.26F

    override var profileTranslation = Vec3(0.0, 0.8800000000000001, 0.0)
    override var profileScale = 0.55000013F

    lateinit var fly: Pose
    lateinit var flyidle: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("dartrix", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES,
            transformedParts = arrayOf(
                wingsOpen.createTransformation().withVisibility(false),
                wingsClosed.createTransformation().withVisibility(true)
            ),
            animations = arrayOf(
                bedrock("dartrix", "ground_idle")
            )
        )

        flyidle = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            transformedParts = arrayOf(
                wingsOpen.createTransformation().withVisibility(true),
                wingsClosed.createTransformation().withVisibility(false)
            ),
            animations = arrayOf(
                bedrock("dartrix", "ground_idle"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = -10F.toRadians(), period = 0.9F, amplitude = 0.6F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Z_AXIS
                )
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            transformTicks = 10,
            transformedParts = arrayOf(
                wingsOpen.createTransformation().withVisibility(true),
                wingsClosed.createTransformation().withVisibility(false)
            ),
            animations = arrayOf(
                bedrock("dartrix", "ground_idle"),
                WingFlapIdleAnimation(this,
                    flapFunction = sineFunction(verticalShift = -14F.toRadians(), period = 0.9F, amplitude = 0.9F),
                    timeVariable = { state, _, _ -> state.animationSeconds },
                    axis = ModelPartTransformation.Z_AXIS
                )
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES - PoseType.FLY,
            transformedParts = arrayOf(
                wingsOpen.createTransformation().withVisibility(false),
                wingsClosed.createTransformation().withVisibility(true)
            ),
            animations = arrayOf(
                bedrock("dartrix", "ground_idle"),
                BipedWalkAnimation(this, periodMultiplier = 0.75F, amplitudeMultiplier = 0.7F)
                //bedrock("dartrix", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dartrix", "faint") else null
}