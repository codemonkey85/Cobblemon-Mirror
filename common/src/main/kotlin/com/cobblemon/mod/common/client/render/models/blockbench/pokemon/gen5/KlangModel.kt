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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class KlangModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("klang")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(0.9, 0.55, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("klang", "cry") }

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            animations = arrayOf(bedrock("klang", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("klang", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("klang", "ground_walk")
                )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("klang", "faint") else null
}