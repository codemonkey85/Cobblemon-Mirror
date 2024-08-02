/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SpewpaModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("spewpa")
    override val head = getPart("head")

    override var portraitScale = 2.8F
    override var portraitTranslation = Vec3(0.0, -1.8, 0.0)

    override var profileScale = 1.2F
    override var profileTranslation = Vec3(0.0, 0.0, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("spewpa", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("spewpa", "blink") }
        val fluff = quirk(secondsBetweenOccurrences = 60F to 360F) { bedrockStateful("spewpa", "quirk_fluff") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("spewpa", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, fluff),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("spewpa", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("spewpa", "ground_walk")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("spewpa", "faint") else null
}