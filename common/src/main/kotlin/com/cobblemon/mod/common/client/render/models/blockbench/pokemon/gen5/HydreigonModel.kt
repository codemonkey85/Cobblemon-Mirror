/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class HydreigonModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("hydreigon")
    override val head = getPart("neck")

    override var portraitScale = 1.57F
    override var portraitTranslation = Vec3d(-1.04, 1.93, 0.0)

    override var profileScale = 0.58F
    override var profileTranslation = Vec3d(-0.06, 1.2, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

//    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("hydreigon", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("hydreigon", "blink") }

        standing = registerPose(
            poseName = "standing",
            quirks = arrayOf(blink),
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("hydreigon", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            quirks = arrayOf(blink),
            poseTypes = PoseType.MOVING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("hydreigon", "ground_idle")
                //bedrock("hydreigon", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("hydreigon", "faint") else null
}