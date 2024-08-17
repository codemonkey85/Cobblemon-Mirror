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
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class StarmieModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("starmie")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(0.0, -1.0, 0.0)

    override var profileScale = 1.4F
    override var profileTranslation = Vec3(0.0, -0.24, 0.0)

    lateinit var battleidle: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var swim: Pose
    lateinit var float: Pose
    lateinit var sleep: Pose

    override fun registerPoses() {

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("starmie", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("starmie", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("starmie", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("starmie", "water_swim")
            )
        )

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("starmie", "sleep"))
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("starmie", "battle_idle")
            )

        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("starmie", "faint") else null
}