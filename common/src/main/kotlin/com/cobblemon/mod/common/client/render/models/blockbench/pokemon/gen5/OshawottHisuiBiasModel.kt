/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BimanualSwingAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class OshawottHisuiBiasModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("oshawott_hisui_bias")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")
    val scalchop = getPart("scalchop_hand")
    val scalchopbody = getPart("scalchop_torso")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.2, -0.15, 0.0)
    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.69, 0.0)

    lateinit var battleidle: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("oshawott_hisui_bias", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("oshawott_hisui_bias", "blink") }

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformTicks = 10,
                transformedParts = arrayOf(
                        scalchop.createTransformation().withVisibility(visibility = false),
                        scalchopbody.createTransformation().withVisibility(visibility = true),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -7)
                ),
                animations = arrayOf(bedrock("oshawott_hisui_bias", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                transformedParts = arrayOf(
                        scalchop.createTransformation().withVisibility(visibility = false),
                        scalchopbody.createTransformation().withVisibility(visibility = true)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("oshawott_hisui_bias", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walking",
                poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                transformedParts = arrayOf(
                        scalchop.createTransformation().withVisibility(visibility = false),
                        scalchopbody.createTransformation().withVisibility(visibility = true)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("oshawott_hisui_bias", "ground_idle"),
                        BipedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F),
                        BimanualSwingAnimation(this, swingPeriodMultiplier = 0.6F, amplitudeMultiplier = 0.9F)
                )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.FLOAT,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling },
                transformedParts = arrayOf(
                        scalchop.createTransformation().withVisibility(visibility = true),
                        scalchopbody.createTransformation().withVisibility(visibility = false)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("oshawott_hisui_bias", "ground_idle")
                )
        )
    }
}