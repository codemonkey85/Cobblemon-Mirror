/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GoldeenModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("goldeen")

    override var portraitScale = 3.2F
    override var portraitTranslation = Vec3(-0.05, -3.3, 0.0)

    override var profileScale = 1.4F
    override var profileTranslation = Vec3(0.0, -0.5, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            animations = arrayOf(
                bedrock("goldeen", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            animations = arrayOf(
                bedrock("goldeen", "ground_idle")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            animations = arrayOf(
                bedrock("goldeen", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            animations = arrayOf(
                bedrock("goldeen", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("goldeen", "faint") else null
}