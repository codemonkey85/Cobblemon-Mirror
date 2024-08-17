/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveSegment
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class QwilfishModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame  {
    override val rootPart = root.registerChildWithAllChildren("qwilfish")
    override val head = getPart("body")

    val tail = getPart("tail")

    val wtail = WaveSegment(tail, 7F)

    override var portraitScale = 2.12F
    override var portraitTranslation = Vec3(-0.26, -1.06, 0.0)

    override var profileScale = 0.84F
    override var profileTranslation = Vec3(0.02, 0.54, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("qwilfish", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("qwilfish", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("qwilfish", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("qwilfish", "ground_idle"),
                WaveAnimation(
                    waveFunction = sineFunction(
                        period = 8F,
                        amplitude = 0.4F
                    ),
                    basedOnLimbSwing = true,
                    oscillationsScalar = 8F,
                    head = head,
                    rotationAxis = ModelPartTransformation.Y_AXIS,
                    motionAxis = ModelPartTransformation.X_AXIS,
                    headLength = 0.1F,
                    segments = arrayOf(
                        wtail
                    )
                )
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("qwilfish", "faint") else null
}