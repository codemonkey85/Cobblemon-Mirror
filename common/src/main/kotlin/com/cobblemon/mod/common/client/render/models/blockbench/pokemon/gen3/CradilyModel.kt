/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CradilyModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("cradily")
    override val head = getPart("head")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(-0.8, 1.0, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
//    lateinit var waterIdle: CobblemonPose
//    lateinit var waterSwim: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("cradily", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("cradily", "blink") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("cradily", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("cradily", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("cradily", "ground_walk")
            )
        )
//        waterIdle = registerPose(
//            poseName = "water_idle",
//            poseType = PoseType.FLOAT,
//            quirks = arrayOf(blink),
//            animations = arrayOf(
//                singleBoneLook(),
//                bedrock("cradily", "water_idle")
//            )
//        )
//
//        waterSwim = registerPose(
//            poseName = "water_swim",
//            poseType = PoseType.SWIM,
//            quirks = arrayOf(blink),
//            animations = arrayOf(
//                singleBoneLook(),
//                bedrock("cradily", "water_swim")
//            )
//        )
    }
}