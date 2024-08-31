/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.SingleBoneLookAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class TandemausModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("tandemaus")
    override val head = getPart("head")

    override var portraitScale = 1.0F
    override var portraitTranslation = Vec3(0.1, 0.0, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
//    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("tandemaus", "cry") }

    override fun registerPoses() {

        val blink1 = quirk { bedrockStateful("tandemaus", "blink1")}
        val blink2 = quirk { bedrockStateful("tandemaus", "blink2")}

        val head2 = object : HeadedFrame {
            override val rootPart = this@TandemausModel.rootPart
            override val head: ModelPart = getPart("head2")
        }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2),
            animations = arrayOf(
                bedrock("tandemaus", "ground_idle"),
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2),
            animations = arrayOf(
                bedrock("tandemaus", "ground_walk"),
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
            )
        )

//        sleep = registerPose(
//            poseName = "sleep",
//            poseType = PoseType.SLEEP,
//            transformTicks = 10,
//            quirks = arrayOf(blink1, blink2),
//            animations = arrayOf(
//                bedrock("tandemaus", "sleep")
//            )
//        )
    }
    override fun getFaintAnimation(state: PosableState) = bedrockStateful("tandemaus", "faint")
}