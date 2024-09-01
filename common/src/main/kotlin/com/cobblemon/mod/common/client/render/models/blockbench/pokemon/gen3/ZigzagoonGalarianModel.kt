/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ZigzagoonGalarianModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("zigzagoon_galarian")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 2.55F
    override var portraitTranslation = Vec3(-0.5, -2.2, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.2, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    val shoulderOffsetX = 0
    val shoulderOffsetY = 0
    val shoulderOffsetZ = -0.5

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("zigzagoon_galarian", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("zigzagoon_galarian", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                QuadrupedWalkAnimation(this, periodMultiplier = 1.1F),
                singleBoneLook(),
                bedrock("zigzagoon_galarian", "ground_idle")
            )
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("zigzagoon", "shoulder_left")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(shoulderOffsetX, shoulderOffsetY, shoulderOffsetZ)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("zigzagoon", "shoulder_right")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(-shoulderOffsetX, shoulderOffsetY, shoulderOffsetZ)
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("zigzagoon_galarian", "faint") else null
}