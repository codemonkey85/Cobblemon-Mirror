/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class AerodactylModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("aerodactyl")
    override val head = getPart("head")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-1.0, -0.65, 0.0)

    override var profileScale = 0.55F
    override var profileTranslation = Vec3(0.0, 0.55, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var hover: CobblemonPose
    lateinit var fly: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("aerodactyl", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("aerodactyl", "blink")}
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("aerodactyl", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("aerodactyl", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("aerodactyl", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = setOf(PoseType.HOVER, PoseType.FLOAT) + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("aerodactyl", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("aerodactyl", "air_fly")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("aerodactyl", "faint") else null
}