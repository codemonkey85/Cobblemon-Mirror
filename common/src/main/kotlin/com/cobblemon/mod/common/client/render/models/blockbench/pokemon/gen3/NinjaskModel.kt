/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class NinjaskModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("ninjask")

    override val head = getPart("head")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.2, -0.4, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.3, 0.0)

    lateinit var sleep: Pose
    lateinit var stand: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose

    override val cryAnimation = CryProvider { bedrockStateful("ninjask", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("ninjask", "blink") }
        val barrelRoll = quirk { bedrockStateful("ninjask", "run_quirk") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("ninjask", "sleep"))
        )

        stand = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES - PoseType.HOVER,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ninjask", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            quirks = arrayOf(blink, barrelRoll),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ninjask", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            transformTicks = 10,
            quirks = arrayOf(blink, barrelRoll),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ninjask", "air_fly")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            transformTicks = 10,
            quirks = arrayOf(blink, barrelRoll),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ninjask", "ground_walk")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = bedrockStateful("ninjask", "faint")
}