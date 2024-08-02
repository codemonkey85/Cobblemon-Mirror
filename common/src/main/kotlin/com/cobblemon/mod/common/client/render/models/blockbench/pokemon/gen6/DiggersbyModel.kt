/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DiggersbyModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("diggersby")
    override val head = getPart("head")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.15, 1.5, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose
    lateinit var portrait: Pose

    override val cryAnimation = CryProvider { bedrockStateful("diggersby", "cry") }

    override fun registerPoses() {
        val sleep1 = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("diggersby", "quirk_sleep") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(sleep1),
            animations = arrayOf(bedrock("diggersby", "sleep"))
        )

        portrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            animations = arrayOf(bedrock("diggersby", "portrait"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.PROFILE,
            transformTicks = 10,
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("diggersby", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("diggersby", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("diggersby", "battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("diggersby", "faint") else null
}