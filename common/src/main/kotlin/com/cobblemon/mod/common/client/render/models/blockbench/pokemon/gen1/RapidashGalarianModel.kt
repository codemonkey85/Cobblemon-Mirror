/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class RapidashGalarianModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("rapidash_galar")
    override val head = getPart("neck")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 1.86F
    override var portraitTranslation = Vec3(-0.5, 1.3, 0.0)

    override var profileScale = 0.67F
    override var profileTranslation = Vec3(0.0, 0.72, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battle_idle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("rapidash_galar", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("rapidash_galar", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rapidash_galar", "ground_idle"),
                bedrock("rapidash_galar", "hair")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rapidash_galar", "ground_walk"),
                bedrock("rapidash_galar", "hair")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rapidash_galar", "battle_idle"),
                bedrock("rapidash_galar", "hair")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("rapidash_galar", "faint") else null
}