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
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class WeezingModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("weezing")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(0.0, 0.0, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.05, 0.4, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("weezing", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("weezing", "blink")}
        val blink2 = quirk { bedrockStateful("weezing", "blink2")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink, blink2),
            animations = arrayOf(
                bedrock("weezing", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink, blink2),
            animations = arrayOf(
                bedrock("weezing", "ground_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("weezing", "faint") else null
}