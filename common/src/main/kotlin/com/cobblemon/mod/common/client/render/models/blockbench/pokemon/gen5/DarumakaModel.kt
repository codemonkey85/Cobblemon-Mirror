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

class DarumakaModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("darumaka")

    override var portraitScale = 1.96F
    override var portraitTranslation = Vec3(-0.18, -0.84, 0.0)

    override var profileScale = 0.76F
    override var profileTranslation = Vec3(0.0, 0.57, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("darumaka", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("darumaka", "blink") }
        val quirk = quirk(secondsBetweenOccurrences = 15F to 100F) { bedrockStateful("darumaka", "quirk") }
        val quirk2 = quirk(secondsBetweenOccurrences = 20F to 360F) { bedrockStateful( "darumaka","quirk2")}

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("darumaka", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, quirk, quirk2),
                condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("darumaka", "ground_idle")
            )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink, quirk),
                condition = { it.isBattling },
                animations = arrayOf(
                        bedrock("darumaka", "battle_idle")
                )

        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                bedrock("darumaka", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("darumaka", "faint") else null
}