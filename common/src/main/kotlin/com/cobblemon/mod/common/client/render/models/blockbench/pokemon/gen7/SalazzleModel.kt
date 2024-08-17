/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SalazzleModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("salazzle")
    override val head = getPart("head")

    override val leftLeg = getPart("leg_right")
    override val rightLeg = getPart("leg_left")

    override var portraitScale = 1.99F
    override var portraitTranslation = Vec3(-0.15, 2.15, 0.0)

    override var profileScale = 0.61F
    override var profileTranslation = Vec3(0.0, 0.89, -6.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleIdle: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("salazzle", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("salazzle", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("salazzle", "ground_walk")
            )
        )

        battleIdle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("salazzle", "battle_idle")
            )
        )
    }
}