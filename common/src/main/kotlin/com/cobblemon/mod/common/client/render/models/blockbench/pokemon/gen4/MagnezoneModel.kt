/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MagnezoneModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("magnezone")

    override var portraitScale = 1.4F
    override var portraitTranslation = Vec3(-0.35, -0.28, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3(-0.1, 0.6, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose


    override fun registerPoses() {
        val blink = quirk { bedrockStateful("magnezone", "blink") }
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            idleAnimations = arrayOf(bedrock("magnezone", "sleep"))
//        )

        registerPose(
            poseName = "hover",
            poseTypes = PoseType.ALL_POSES - PoseType.FLY - PoseType.SLEEP - PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("magnezone", "ground_idle")
            )
        )

        registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("magnezone", "ground_idle")
            )
        )

    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("magnezone", "faint") else null
}