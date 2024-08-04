/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SprigatitoModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("sprigatito")
    override val head = getPart("head")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3(-0.35, -0.7, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.53, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("sprigatito", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("sprigatito", "blink") }
        val earTwitchRight = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("sprigatito", "quirk_ear-twitch-left") }
        val earTwitchLeft = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("sprigatito", "quirk_ear-twitch-right") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(earTwitchRight, earTwitchLeft),
            animations = arrayOf(bedrock("sprigatito", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink, earTwitchRight, earTwitchLeft),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sprigatito", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, earTwitchRight, earTwitchLeft),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sprigatito", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, earTwitchRight, earTwitchLeft),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sprigatito", "battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("sprigatito", "faint") else null
}