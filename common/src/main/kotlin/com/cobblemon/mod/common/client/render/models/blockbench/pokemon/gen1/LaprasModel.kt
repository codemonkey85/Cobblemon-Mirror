/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class LaprasModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("lapras")
    override val head = getPart("head_ai")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 1.14F
    override var portraitTranslation = Vec3(-0.66, 1.91, 0.0)

    override var profileScale = 0.48F
    override var profileTranslation = Vec3(-0.01, 0.99, 0.0)

    lateinit var landIdle: CobblemonPose
    lateinit var landMove: CobblemonPose
    lateinit var surfaceIdle: CobblemonPose
    lateinit var surfaceMove: CobblemonPose
    lateinit var underwaterIdle: CobblemonPose
    lateinit var underwaterMove: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("lapras", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("lapras", "blink") }
        landIdle = registerPose(
            poseName = "land_idle",
            poseTypes = UI_POSES + PoseType.STAND,
            condition = { !it.isInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lapras", "ground_idle")
            )
        )

        landMove = registerPose(
            poseName = "land_move",
            poseTypes = setOf(PoseType.WALK),
            condition = { !it.isInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lapras", "ground_idle"),
                QuadrupedWalkAnimation(this, 2.5F, 0.25F )
            )
        )

        surfaceIdle = registerPose(
            poseName = "surface_idle",
            poseTypes = setOf(PoseType.STAND),
            condition = { it.isInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lapras", "surfacewater_idle")
            )
        )

        surfaceMove = registerPose(
            poseName = "surface_move",
            poseTypes = setOf(PoseType.WALK),
            condition = { it.isInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lapras", "surfacewater_swim")
            )
        )

        underwaterIdle = registerPose(
            poseName = "underwater_idle",
            poseTypes = setOf(PoseType.FLOAT),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lapras", "surfacewater_idle")
            )
        )

        underwaterMove = registerPose(
            poseName = "underwater_move",
            poseTypes = setOf(PoseType.SWIM),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("lapras", "surfacewater_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(landIdle, landMove)) bedrockStateful("lapras", "faint") else null
}