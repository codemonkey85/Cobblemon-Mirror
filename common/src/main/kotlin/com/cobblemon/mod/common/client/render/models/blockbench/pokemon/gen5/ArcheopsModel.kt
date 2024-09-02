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

class ArcheopsModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("archeops")
    override val head = getPart("neck")

    override var portraitTranslation = Vec3(-1.19, 1.15, 0.0)
    override var portraitScale = 1.51F

    override var profileTranslation = Vec3(-0.04, 0.97, -6.0)
    override var profileScale = 0.49F

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var hovering: CobblemonPose
    lateinit var flying: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleIdle: CobblemonPose

    override val cryAnimation = CryProvider { if (it.isPosedIn(hovering, flying)) bedrockStateful("archeops", "air_cry") else bedrockStateful("archeops", "cry") }


    override fun registerPoses() {
        val blink = quirk { bedrockStateful("archeops", "blink") }
        val quirk1 = quirk { bedrockStateful("archeops", "quirk") }
        val quirk2 = quirk { bedrockStateful("archeops", "quirk2") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("archeops", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.HOVER,
            condition = { !it.isBattling && !it.isInWater },
            quirks = arrayOf(blink, quirk1, quirk2),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            condition = { !it.isInWater },
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "ground_walk")
            )
        )

        hovering = registerPose(
            poseName = "hovering",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink, quirk1, quirk2),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "air_idle")
            )
        )

        flying = registerPose(
            poseName = "flying",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "air_fly")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "surfacewater_swim")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, quirk1),
            condition = { it.isInWater },
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "surfacewater_idle")
            )
        )

        battleIdle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, quirk2),
            condition = { it.isBattling },
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archeops", "battle_idle")
            )
        )

    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking)) bedrockStateful("archeops", "faint") else null
}