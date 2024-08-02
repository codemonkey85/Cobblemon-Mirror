/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class NickitModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("nickit")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.3, -0.1, 0.0)

    override var profileScale = 0.75F
    override var profileTranslation = Vec3(0.0, 0.65, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("nickit", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("nickit", "blink")}
        val glanceleft = quirk { bedrockStateful("nickit", "quirk_shiftyglance_left")}
        val glanceright = quirk { bedrockStateful("nickit", "quirk_shiftyglance_right")}
        val eartwitchleft = quirk { bedrockStateful("nickit", "quirk_eartwitch_left")}
        val eartwitchright = quirk { bedrockStateful("nickit", "quirk_eartwitch_right")}

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("nickit", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, glanceleft, glanceright, eartwitchleft, eartwitchright),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("nickit", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, eartwitchleft, eartwitchright),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("nickit", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, glanceleft, glanceright, eartwitchleft, eartwitchright),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("nickit", "battle_idle")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep, battleidle)) bedrockStateful("nickit", "faint") else null
}