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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MausholdModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("maushold")
    override val head = getPart("head")

    override var portraitScale = 1.0F
    override var portraitTranslation = Vec3(0.1, 0.0, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("maushold_three", "cry") }

    override fun registerPoses() {

        val blink1 = quirk { bedrockStateful("maushold_three", "blink1")}
        val blink2 = quirk { bedrockStateful("maushold_three", "blink2")}
        val blink3 = quirk { bedrockStateful("maushold_four", "blink3")}

        val head2 = object : HeadedFrame {
            override val rootPart = this@MausholdModel.rootPart
            override val head: ModelPart = getPart("head2")
        }

        val head3 = object : HeadedFrame {
            override val rootPart = this@MausholdModel.rootPart
            override val head: ModelPart = getPart("head3")
        }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3),
            animations = arrayOf(
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
                SingleBoneLookAnimation(head3, false, false, disableX = false, disableY = false),
                bedrock("maushold_three", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3),
            animations = arrayOf(
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
                SingleBoneLookAnimation(head3, false, false, disableX = false, disableY = false),
                bedrock("maushold_three", "ground_walk")
            )
        )

        //sleep = registerPose(
        //    poseName = "sleep",
        //    poseType = PoseType.SLEEP,
        //    transformTicks = 10,
        //    animations = arrayOf(
        //        bedrock("maushold_three", "sleep")
        //    )
        //)
    }
    override fun getFaintAnimation(state: PosableState) = bedrockStateful("maushold_three", "faint")
}