/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DewottHisuiBiasModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("dewott_hisui_bias")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")
    val scalchop_body_right = getPart("scalchop_skirt_right")
    val scalchop_body_left = getPart("scalchop_skirt_left")
    val scalchop_right = getPart("scalchop_hand_right")
    val scalchop_left = getPart("scalchop_hand_left")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.15, 0.8, 0.0)
    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.69, 0.0)

    lateinit var battleidle: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("dewott_hisui_bias", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dewott_hisui_bias", "blink") }
        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformTicks = 10,
                condition = { !it.isBattling },
                transformedParts = arrayOf(
                        scalchop_right.createTransformation().withVisibility(visibility = false),
                        scalchop_left.createTransformation().withVisibility(visibility = false),
                        scalchop_body_right.createTransformation().withVisibility(visibility = true),
                        scalchop_body_left.createTransformation().withVisibility(visibility = true)

                ),
                animations = arrayOf(bedrock("dewott_hisui_bias", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                transformedParts = arrayOf(
                        scalchop_right.createTransformation().withVisibility(visibility = false),
                        scalchop_left.createTransformation().withVisibility(visibility = false),
                        scalchop_body_right.createTransformation().withVisibility(visibility = true),
                        scalchop_body_left.createTransformation().withVisibility(visibility = true)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("dewott_hisui_bias", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walking",
                poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                transformedParts = arrayOf(
                        scalchop_right.createTransformation().withVisibility(visibility = false),
                        scalchop_left.createTransformation().withVisibility(visibility = false),
                        scalchop_body_right.createTransformation().withVisibility(visibility = true),
                        scalchop_body_left.createTransformation().withVisibility(visibility = true)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("dewott_hisui_bias", "ground_walk")
                )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling },
                transformedParts = arrayOf(
                        scalchop_right.createTransformation().withVisibility(visibility = true),
                        scalchop_left.createTransformation().withVisibility(visibility = true),
                        scalchop_body_right.createTransformation().withVisibility(visibility = false),
                        scalchop_body_left.createTransformation().withVisibility(visibility = false)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("dewott_hisui_bias", "ground_idle")
                )

        )
    }
}