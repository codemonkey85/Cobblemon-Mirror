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
import com.cobblemon.mod.common.entity.PoseType.Companion.STANDING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DewgongModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dewgong")
    override val head = getPart("head")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.6, -2.0, 0.0)

    override var profileScale = 1.1F
    override var profileTranslation = Vec3(0.0, 0.0, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var swim: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("dewgong", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STANDING_POSES + UI_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dewgong", "ground_idle")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dewgong", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dewgong", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dewgong", "faint") else null
}