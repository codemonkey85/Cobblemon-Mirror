/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class OmastarModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("omastar")

    override var portraitTranslation = Vec3(0.02, -2.20, 0.0)
    override var portraitScale = 2.4F

    override var profileTranslation = Vec3(0.0, 0.066, 0.0)
    override var profileScale = 1.1F

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("omastar", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("omastar", "blink") }
        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("omastar", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            transformTicks = 10,
            condition = { !it.isBattling},
            animations = arrayOf(
                bedrock("omastar", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                bedrock("omastar", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                bedrock("omastar", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseTypes = setOf(PoseType.SWIM, PoseType.FLY),
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                bedrock("omastar", "water_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battle-standing",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            transformTicks = 10,
            condition = { it.isBattling},
            animations = arrayOf(
                bedrock("omastar", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("omastar", "faint") else null
}