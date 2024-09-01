/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.SHOULDER_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ChinglingModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("chingling")

    override var portraitScale = 2.8F
    override var portraitTranslation = Vec3(0.0, -2.4, 0.0)

    override var profileScale = 1.3F
    override var profileTranslation = Vec3(0.0, -0.2, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var hover: CobblemonPose
    lateinit var fly: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("chingling", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("chingling", "blink") }
        val sleepquirk = quirk { bedrockStateful("chingling", "sleep_quirk") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(sleepquirk),
            animations = arrayOf(bedrock("chingling", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES + SHOULDER_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chingling", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chingling", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chingling", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chingling", "air_fly")
            )
        )
    }

    //override fun getFaintAnimation(
    //    pokemonEntity: PokemonEntity,
    //    state: PosableState<PokemonEntity>
    //) = if (state.isPosedIn(standing, walk, hover, fly, sleep )) bedrockStateful("chingling", "faint") else null
}