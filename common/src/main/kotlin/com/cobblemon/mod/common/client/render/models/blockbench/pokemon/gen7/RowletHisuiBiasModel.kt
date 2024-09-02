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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class RowletHisuiBiasModel(root: ModelPart) : PokemonPosableModel(root), BipedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("rowlet_hisui_bias")

    private val wingsOpen = getPart("wings_open")
    private val wingsClosed = getPart("wings_folded")

    override val leftWing = getPart("wing_open_left_main")
    override val rightWing = getPart("wing_open_right_main")

    override val leftLeg = getPart("foot_left")
    override val rightLeg = getPart("foot_right")

    override var portraitTranslation = Vec3(-0.3, -0.34, 0.0)
    override var portraitScale = 1.77F

    override var profileTranslation = Vec3(-0.04, 0.58, 0.0)
    override var profileScale = 0.76F

    lateinit var fly: CobblemonPose
    lateinit var flyidle: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("rowlet_hisui_bias", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("rowlet_hisui_bias", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES,
                transformedParts = arrayOf(
                        wingsOpen.createTransformation().withVisibility(false),
                        wingsClosed.createTransformation().withVisibility(true)
                ),
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("rowlet_hisui_bias", "ground_idle")
                )
        )

        flyidle = registerPose(
                poseName = "hover",
                poseType = PoseType.HOVER,
                transformTicks = 10,
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        wingsOpen.createTransformation().withVisibility(true),
                        wingsClosed.createTransformation().withVisibility(false)
                ),
                animations = arrayOf(
                        bedrock("rowlet_hisui_bias", "flying_idle"),
                        WingFlapIdleAnimation(this,
                                flapFunction = sineFunction(verticalShift = -8F.toRadians(), period = 1.0F, amplitude = 0.4F),
                                timeVariable = { state, _, _ -> state.animationSeconds },
                                axis = ModelPartTransformation.Z_AXIS
                        )
                )
        )

        fly = registerPose(
                poseName = "fly",
                poseType = PoseType.FLY,
                transformTicks = 10,
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        wingsOpen.createTransformation().withVisibility(true),
                        wingsClosed.createTransformation().withVisibility(false)
                ),
                animations = arrayOf(
                        bedrock("rowlet_hisui_bias", "flying_idle"),
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
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        wingsOpen.createTransformation().withVisibility(false),
                        wingsClosed.createTransformation().withVisibility(true)
                ),
                animations = arrayOf(
                        bedrock("rowlet_hisui_bias", "ground_idle"),
                        BipedWalkAnimation(this, periodMultiplier = 0.8F, amplitudeMultiplier = 0.7F)
                        //bedrock("rowlet_hisui_bias", "ground_walk")
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("rowlet_hisui_bias", "faint") else null
}