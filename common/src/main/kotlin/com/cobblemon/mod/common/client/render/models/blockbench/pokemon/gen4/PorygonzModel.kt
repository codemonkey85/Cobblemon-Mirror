/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import kotlin.random.Random
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PorygonzModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("porygon_z")
    override val head = getPart("head")

    override var portraitScale = 2.4F
    override var portraitTranslation = Vec3(-0.1, 1.1, 0.0)

    override var profileScale = 0.85F
    override var profileTranslation = Vec3(0.0, 0.6, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("porygonz", "blink") }
        val twitch1 = quirk { bedrockStateful("porygonz", "twitch1") }
        val twitch2 = quirk { bedrockStateful("porygonz", "twitch2") }
        val twitch3 = quirk { bedrockStateful("porygonz", "twitch3") }
        val twitch4 = quirk { bedrockStateful("porygonz", "twitch4") }
        val twitch5 = quirk { bedrockStateful("porygonz", "twitch5") }
        val twitch6 = quirk { bedrockStateful("porygonz", "twitch6") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(twitch1, twitch2, twitch3, twitch4, twitch5, twitch6),
            animations = arrayOf(bedrock("porygonz", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink, twitch1, twitch2, twitch3, twitch4, twitch5, twitch6),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("porygonz", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink, twitch1, twitch2, twitch3, twitch4, twitch5, twitch6),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("porygonz", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) =
        if ((Random.Default.nextBoolean())) bedrockStateful("porygonz", "faint")
        else bedrockStateful("porygonz", "faint2")
}