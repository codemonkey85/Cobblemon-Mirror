/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ArctozoltModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("arctozolt")
    override val head = getPart("head_ai")

    override var portraitTranslation = Vec3(-0.45, 2.15, 0.0)
    override var portraitScale = 0.85F

    override var profileTranslation = Vec3(0.02, 0.95, 0.0)
    override var profileScale = 0.33F

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var surfacefloat: CobblemonPose
    lateinit var surfaceswim: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var water_sleep: CobblemonPose
    lateinit var surface_sleep: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("arctozolt", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("arctozolt", "blink") }
        val quirk = quirk (secondsBetweenOccurrences = 30F to 60f) { bedrockStateful("arctozolt", "quirk") }

        sleep = registerPose(
                poseName = "sleep",
                poseType = PoseType.SLEEP,
                condition = { !it.isInWater },
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("arctozolt", "sleep")
                )
        )

        surface_sleep = registerPose(
                poseName = "surface_sleep",
                poseType = PoseType.SLEEP,
                condition = { !it.isUnderWater && it.isInWater},
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("arctozolt", "surfacewater_sleep")
                )
        )

        water_sleep = registerPose(
                poseName = "water_sleep",
                poseType = PoseType.SLEEP,
                condition = { it.isInWater && it.isUnderWater},
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("arctozolt", "surfacewater_sleep")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -53)
                )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.FLOAT,
            condition = { !it.isBattling && !it.isInWater},
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            condition = { !it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            condition = { it.isUnderWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            condition = { it.isUnderWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "water_swim")
            )
        )

        surfacefloat = registerPose(
            poseName = "surfacefloat",
            poseType = PoseType.STAND,
            condition = { !it.isUnderWater && it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "surfacewater_idle")
            )
        )

        surfaceswim = registerPose(
            poseName = "surfaceswim",
            poseType = PoseType.WALK,
            condition = { !it.isUnderWater && it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "surfacewater_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("arctozolt", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("arctozolt", "faint") else null
}