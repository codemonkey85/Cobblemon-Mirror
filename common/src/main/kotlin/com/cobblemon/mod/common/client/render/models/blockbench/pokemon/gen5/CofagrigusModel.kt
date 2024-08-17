/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CofagrigusModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("cofagrigus")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(0.0, 2.8, 0.0)

    override var profileScale = 0.35F
    override var profileTranslation = Vec3(0.0, 1.3, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("cofagrigus", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("cofagrigus", "blink") }

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("cofagrigus", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("cofagrigus", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES - PoseType.FLYING_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("cofagrigus", "ground_walk")
                )
        )

        fly = registerPose(
            poseName = "flying",
            poseTypes = PoseType.FLYING_POSES - PoseType.HOVER,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("cofagrigus", "ground_run")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("cofagrigus", "faint") else null
}