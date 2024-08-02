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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DiglettAlolanModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("diglett_alolan")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.06, -0.86, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.32, 0.0)

    lateinit var stand: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var sleep: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("diglett_alolan", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("diglett_alolan", "blink")}
        val quirk = quirk { bedrockStateful("diglett_alolan", "quirk_idle")}

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("diglett_alolan", "sleep"))
        )

        stand = registerPose(
            poseName = "stand",
            poseTypes = STATIONARY_POSES + UI_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink,quirk),
            animations = arrayOf(bedrock("diglett_alolan", "ground_idle"))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(bedrock("diglett_alolan", "ground_idle"))
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = STATIONARY_POSES,
            condition = { it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(bedrock("diglett_alolan", "battle_idle"))
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("diglett_alolan", "faint")
}