/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PinecoModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("pineco")

    override var portraitScale = 1.65F
    override var portraitTranslation = Vec3(-0.15, -0.6, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.58, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("pineco", "blink")}
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("pineco", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("pineco", "ground_walk")
                )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformTicks = 10,
                animations = arrayOf(bedrock("pineco", "sleep"))
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("pineco", "faint") else null
}