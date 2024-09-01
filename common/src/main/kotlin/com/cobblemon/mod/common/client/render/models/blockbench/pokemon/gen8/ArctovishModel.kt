/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ArctovishModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("arctovish")
    override val head = getPart("head")

    override var portraitScale = 0.66F
    override var portraitTranslation = Vec3(-0.36, 1.98, 0.0)

    override var profileScale = 0.35F
    override var profileTranslation = Vec3(0.0, 1.25, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var water_sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var ui_poses: CobblemonPose

    override val cryAnimation = CryProvider {
        when {
            it.isPosedIn(float, swim ) -> bedrockStateful("arctovish", "water_cry")
            else -> bedrockStateful("arctovish", "cry")
        }
    }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("arctovish", "blink") }
        val waterquirk = quirk (secondsBetweenOccurrences = 30F to 60f) { bedrockStateful("arctovish", "water_quirk") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            transformTicks = 10,
            animations = arrayOf(
                bedrock("arctovish", "sleep")
            )
        )

        water_sleep = registerPose(
                poseName = "water_sleep",
                poseType = PoseType.SLEEP,
                condition = { it.isInWater },
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("arctovish", "water_sleep")
                )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.UI_POSES - PoseType.FLOAT,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("arctovish", "ground_idle")
            )
        )

        ui_poses = registerPose(
                poseName = "ui_poses",
                poseTypes = PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("arctovish", "water_idle")
                )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("arctovish", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            quirks = arrayOf(blink, waterquirk),
            animations = arrayOf(
                bedrock("arctovish", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink, waterquirk),
            animations = arrayOf(
                bedrock("arctovish", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("arctovish", "faint") else null
}