/*
 * Copyright (C) 2022 Pokemod Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.gen1

import com.cablemc.pokemod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cablemc.pokemod.common.entity.PoseType
import com.cablemc.pokemod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class SlowpokeModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("slowpoke")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("leftfrontleg")
    override val foreRightLeg = getPart("rightfrontleg")
    override val hindLeftLeg = getPart("leftbackleg")
    override val hindRightLeg = getPart("rightbackleg")

    override val portraitScale = 1.0F
    override val portraitTranslation = Vec3d(0.0, 0.0, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var float: PokemonPose
    lateinit var swim: PokemonPose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0079_slowpoke/slowpoke", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                QuadrupedWalkAnimation(this, periodMultiplier = 1.1F),
                singleBoneLook(),
                bedrock("0079_slowbro/slowpoke", "ground_idle")
                //bedrock("0079_slowpoke/slowpoke", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0079_slowpoke/slowpoke", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0079_slowpoke/slowpoke", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("0079_slowpoke/slowpoke", "faint") else null
}