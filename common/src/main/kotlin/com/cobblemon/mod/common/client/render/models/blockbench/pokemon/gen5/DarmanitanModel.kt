/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DarmanitanModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("darmanitan")

    override var portraitScale = 0.96F
    override var portraitTranslation = Vec3(-0.35, 0.71, 0.0)

    override var profileScale = 0.57F
    override var profileTranslation = Vec3(-0.11, 0.73, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("darmanitan", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("darmanitan", "blink") }
        val quirk = quirk(secondsBetweenOccurrences = 15F to 100F) { bedrockStateful("darmanitan", "quirk") }

        sleep = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("darmanitan", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, quirk),
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("darmanitan", "ground_idle")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("darmanitan", "battle_idle")
            )

        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("darmanitan", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("darmanitan", "faint") else null
}