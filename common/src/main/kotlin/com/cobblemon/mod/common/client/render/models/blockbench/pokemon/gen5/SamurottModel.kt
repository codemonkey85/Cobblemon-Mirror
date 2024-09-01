/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SamurottModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("samurott")
    override val head = getPart("head")
    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")
    val seamitar_right = getPart("seamitar_hand_right")
    val seamitar_left = getPart("seamitar_hand_left")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(-0.91, 1.62, 0.0)
    override var profileScale = 0.6F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("samurott", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("samurott", "blink") }
        //sleep = registerPose(
        //    poseType = PoseType.SLEEP,
        //    transformTicks = 10,
        //    animations = arrayOf(bedrock("samurott", "sleep"))
        //)

        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
            transformTicks = 10,
            quirks = arrayOf(blink),
                condition = { !it.isBattling },
            transformedParts = arrayOf(
                seamitar_right.createTransformation().withVisibility(visibility = false),
                seamitar_left.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("samurott", "ground_idle")
            )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling },
                transformedParts = arrayOf(
                        seamitar_right.createTransformation().withVisibility(visibility = false),
                        seamitar_left.createTransformation().withVisibility(visibility = false)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("samurott", "battle_pose")
                )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
            transformTicks = 10,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                seamitar_right.createTransformation().withVisibility(visibility = false),
                seamitar_left.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("samurott", "ground_idle"),
                QuadrupedWalkAnimation(this, periodMultiplier = 0.8F, amplitudeMultiplier = 0.8F)
            )
        )
    }
}