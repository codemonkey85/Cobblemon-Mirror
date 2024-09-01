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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class AmauraModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("amaura")
    override val head = getPart("neck")

    override var portraitScale = 1.82F
    override var portraitTranslation = Vec3(-0.64, 1.24, 0.0)

    override var profileScale = 0.55F
    override var profileTranslation = Vec3(0.0, 0.88, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var sleep: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("amaura", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("amaura", "blink") }
        val quirk = quirk(secondsBetweenOccurrences = 60F to 360F) { bedrockStateful("amaura", "quirk_idle") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("amaura", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("amaura", "ground_idle")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            condition = { it.isBattling },
            animations = arrayOf(
                    singleBoneLook(),
                    bedrock("amaura", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("amaura", "ground_walk")
            )
        )

    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("amaura", "faint")
}