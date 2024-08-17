/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ChimechoModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("chimecho")

    override var portraitScale = 3.6F
    override var portraitTranslation = Vec3(-0.1, -0.4, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.3, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var hover: CobblemonPose
    lateinit var fly: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("chimecho", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("chimecho", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("chimecho", "sleep"))
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chimecho", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "flying",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chimecho", "air_fly")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(hover, fly, sleep)) bedrockStateful("chimecho", "faint") else null
}