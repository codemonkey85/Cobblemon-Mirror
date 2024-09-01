/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GrowlitheModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("growlithe")
    override val head = getPart("neck")

    override val foreLeftLeg= getPart("left_shoulder")
    override val foreRightLeg = getPart("right_shoulder")
    override val hindLeftLeg = getPart("left_thigh")
    override val hindRightLeg = getPart("right_thigh")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3(-0.2, -0.6, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

//    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("growlithe", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("growlithe", "blink") }
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            animations = arrayOf(bedrock("growlithe", "sleep"))
//        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("growlithe", "idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("growlithe", "idle"),
                QuadrupedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("growlithe", "faint") else null
}