/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class NickitModel (root: ModelPart) : PosableModel(), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("nickit")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override val portraitScale = 1.8F
    override val portraitTranslation = Vec3d(-0.3, -0.1, 0.0)

    override val profileScale = 0.75F
    override val profileTranslation = Vec3d(0.0, 0.65, 0.0)

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
            idleAnimations = arrayOf(bedrock("nickit", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, glanceleft, glanceright, eartwitchleft, eartwitchright),
            condition = { (it.entity as? PokemonEntity)?.isBattling == false },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("nickit", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, eartwitchleft, eartwitchright),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("nickit", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, glanceleft, glanceright, eartwitchleft, eartwitchright),
            condition = { (it.entity as? PokemonEntity)?.isBattling == true },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("nickit", "battle_idle")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep, battleidle)) bedrockStateful("nickit", "faint") else null
}