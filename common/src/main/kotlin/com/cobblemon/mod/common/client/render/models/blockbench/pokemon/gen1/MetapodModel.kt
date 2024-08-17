/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.ALL_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MetapodModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("metapod")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.15, -1.25, 0.0)
    override var profileScale = 1.1F
    override var profileTranslation = Vec3(0.0, 0.1, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose

    override val cryAnimation = CryProvider { bedrockStateful("metapod", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("metapod", "blink")}
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("metapod", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            quirks = arrayOf(blink),
            poseTypes = ALL_POSES - PoseType.SLEEP,
            animations = arrayOf(bedrock("metapod", "ground_idle"))
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("metapod", "faint")
}