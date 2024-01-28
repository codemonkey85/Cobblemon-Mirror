/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class TentacoolModel(root: ModelPart) : PosableModel() {
    override val rootPart = root.registerChildWithAllChildren("tentacool")

    override val portraitScale = 1.2F
    override val portraitTranslation = Vec3d(0.1, 1.1, 0.0)

    override val profileScale = 0.6F
    override val profileTranslation = Vec3d(0.0, 0.8, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var swim: Pose
    lateinit var float: Pose
    lateinit var sleep: Pose
    lateinit var watersleep: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("tentacool", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacool", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacool", "ground_idle")
                //bedrock("tentacool", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacool", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacool", "water_swim")
            )
        )

        sleep = registerPose(
                poseName = "sleep",
                poseType = PoseType.SLEEP,
                condition = { it.entity?.isTouchingWater == false },
                idleAnimations = arrayOf(bedrock("tentacool", "ground_sleep"))
        )

        watersleep = registerPose(
                poseName = "watersleep",
                poseType = PoseType.SLEEP,
                condition = { it.entity?.isTouchingWater == true },
                idleAnimations = arrayOf(bedrock("tentacool", "water_sleep"))
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("tentacool", "faint") else null
}