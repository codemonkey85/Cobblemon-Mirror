/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ShroomishModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("shroomish")
    override val head = getPart("torso")

    override val leftLeg = getPart("foot_left")
    override val rightLeg = getPart("foot_right")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3d(-0.1, -1.2, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3d(0.0, 0.3, 0.0)

    lateinit var standing: PokemonPose
    lateinit var battling: PokemonPose
    lateinit var sleeping: PokemonPose
    lateinit var walk: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("shroomish", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("shroomish", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F),
                bedrock("shroomish", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F),
                bedrock("shroomish", "ground_walk")
            )
        )

        battling = registerPose(
            poseName = "battlestanding",
            poseTypes = STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F),
                bedrock("shroomish", "battle_idle")
            )
        )

        sleeping = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            idleAnimations = arrayOf(
                bedrock("shroomish", "sleep")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk, battling, sleeping)) bedrockStateful("shroomish", "faint") else null
}