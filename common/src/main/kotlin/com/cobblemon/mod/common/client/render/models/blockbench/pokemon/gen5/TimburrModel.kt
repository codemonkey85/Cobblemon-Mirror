/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class TimburrModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("timburr")
    override val head = getPart("head")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.1, 0.0, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.76, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("timburr", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("timburr", "blink") }
        val quirk = quirk { PrimaryAnimation(bedrockStateful("timburr", "idle_quirk") )}

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("timburr", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("timburr", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("timburr", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("timburr", "battle_idle")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("timburr", "faint")
}