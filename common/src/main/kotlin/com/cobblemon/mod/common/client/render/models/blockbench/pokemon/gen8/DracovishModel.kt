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
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DracovishModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dracovish")
    override val head = getPart("head_ai")

    override var portraitTranslation = Vec3(-0.25, 1.90, 0.0)
    override var portraitScale = 0.55F

    override var profileScale = 0.3F
    override var profileTranslation = Vec3(0.0, 1.35, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var waterbattleidle: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var watersleep: CobblemonPose

    override val cryAnimation = CryProvider {
        when {
            it.isPosedIn(float, swim, waterbattleidle ) -> bedrockStateful("dracovish", "water_cry")
            else -> bedrockStateful("dracovish", "cry")
        }
    }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dracovish", "blink") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dracovish", "sleep")
            )
        )

        watersleep = registerPose(
            poseName = "watersleep",
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dracovish", "watersleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.FLOAT,
            condition = { !it.isBattling },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dracovish", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dracovish", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dracovish", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dracovish", "water_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dracovish", "battle_idle")
            )
        )

        waterbattleidle = registerPose(
            poseName = "waterbattleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling && it.isInWater},
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dracovish", "water_battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dracovish", "faint") else null
}