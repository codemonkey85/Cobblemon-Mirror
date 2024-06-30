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
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class StaryuModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("staryu")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3d(-0.1, -1.25, 0.0)

    override var profileScale = 1.2F
    override var profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var swim: Pose
    lateinit var float: Pose
    lateinit var sleep: Pose

    override fun registerPoses() {

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            animations = arrayOf(
                bedrock("staryu", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            animations = arrayOf(
                bedrock("staryu", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            animations = arrayOf(
                bedrock("staryu", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            animations = arrayOf(
                bedrock("staryu", "water_swim")
            )
        )

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("staryu", "sleep"))
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("staryu", "faint") else null
}