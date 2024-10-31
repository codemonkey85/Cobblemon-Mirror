/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class BuizelModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("buizel")
    override val head = getPart("head")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3(-0.2, 0.1, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.65, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var waterSleep: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var surfaceWaterIdle: CobblemonPose
    lateinit var surfaceWaterSwim: CobblemonPose
    lateinit var surfaceWaterSleep: CobblemonPose

    val wateroffset = -8

    override val cryAnimation = CryProvider { bedrockStateful("buizel", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("buizel", "blink") }
        sleep = registerPose(
            poseName = "sleep",
            condition = { !it.isInWater},
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("buizel", "sleep"))
        )

        waterSleep = registerPose(
            poseName = "water_sleep",
            condition = { it.isUnderWater },
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("buizel", "water_sleep"))
        )

        surfaceWaterSleep = registerPose(
            poseName = "surface_water_sleep",
            condition = { it.isInWater && !it.isUnderWater },
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("buizel", "surfacewater_sleep")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.FLOAT,
            condition = { !it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("buizel", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            condition = { !it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("buizel", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("buizel", "water_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("buizel", "water_swim")
            )
        )

        surfaceWaterIdle = registerPose(
            poseName = "surface_water_idle",
            condition = { it.isInWater && !it.isUnderWater },
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("buizel", "surfacewater_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        surfaceWaterSwim = registerPose(
            poseName = "surface_water_swim",
            condition = { it.isInWater && !it.isUnderWater },
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("buizel", "surfacewater_swim")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, sleep)) bedrockStateful("buizel", "faint") else null
}