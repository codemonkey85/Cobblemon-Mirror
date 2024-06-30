/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class SlugmaShinyModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("slugma_shiny")
    override val head = getPart("head")

    override var portraitScale = 1.7F
    override var portraitTranslation = Vec3d(-0.35, -0.3, 0.0)

    override var profileScale = 0.75F
    override var profileTranslation = Vec3d(0.0, 0.575, 0.0)

    lateinit var shiny_sleep: Pose
    lateinit var shiny_standing: Pose
    lateinit var shiny_walk: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("slugma_shiny", "blink") }

        shiny_sleep = registerPose(
            poseName = "shiny_sleeping",
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("slugma_shiny", "sleep"))
        )

        shiny_standing = registerPose(
            poseName = "shiny_standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("slugma_shiny", "ground_idle")
            )
        )

        shiny_walk = registerPose(
            poseName = "shiny_walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("slugma_shiny", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("slugma_shiny", "faint") else null
}