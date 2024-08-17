/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ElectabuzzModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("electabuzz")
    override val head = getPart("head")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.3, 0.2, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.6, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var sleepambient: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("electabuzz", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("electabuzz", "blink") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            poseName = "sleep",
            condition = { it.isBattling },
            animations = arrayOf(bedrock("electabuzz", "sleep"))
        )

        sleepambient = registerPose(
            poseType = PoseType.SLEEP,
            poseName = "ambientsleep",
            condition = { !it.isBattling },
            animations = arrayOf(bedrock("electabuzz", "ambient_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("electabuzz", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("electabuzz", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("electabuzz", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("electabuzz", "faint") else null
}