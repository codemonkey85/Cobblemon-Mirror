/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class VaporeonModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("vaporeon")
    override val head = getPart("head")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3(-0.6, -0.58, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.35, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var swimIdle: Pose
    lateinit var swimMove: Pose

    override val cryAnimation = CryProvider { bedrockStateful("vaporeon", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("vaporeon", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = setOf(PoseType.WALK),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("vaporeon", "ground_run")
            )
        )

        swimIdle = registerPose(
            poseName = "float",
            poseTypes = setOf(PoseType.FLOAT),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("vaporeon", "water_idle")
            )
        )

        swimMove = registerPose(
            poseName = "swim",
            poseTypes = setOf(PoseType.SWIM),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("vaporeon", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("vaporeon", "faint") else null
}