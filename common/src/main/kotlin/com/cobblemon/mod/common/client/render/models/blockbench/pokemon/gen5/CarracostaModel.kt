/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CarracostaModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("carracosta")
    override val head = getPart("head")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-0.5, 1.0, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.55, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var waterSleep: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var battleIdle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("carracosta", "cry") }

    override fun registerPoses() {
        val blink = quirk {bedrockStateful("carracosta", "blink")}

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            animations = arrayOf(
                bedrock("carracosta", "sleep")
            )
        )

        waterSleep = registerPose(
            poseName = "water_sleep",
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            animations = arrayOf(
                bedrock("carracosta", "water_sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.FLOAT,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("carracosta", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("carracosta", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("carracosta", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("carracosta", "water_swim")
            )
        )

        battleIdle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("carracosta", "battle_idle")
            )
        )
    }
}