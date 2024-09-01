/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PidgeotModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("pidgeot")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val head = getPart("neck")

    private val wingOpenRight = getPart("wing_open_right")
    private val wingOpenLeft = getPart("wing_open_left")
    private val wingClosedRight = getPart("wing_closed_right")
    private val wingClosedLeft = getPart("wing_closed_left")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3(-0.6, 0.15, 0.0)
    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

//    lateinit var sleep: CobblemonPose
    lateinit var stand: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var hover: CobblemonPose
    lateinit var fly: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("pidgeot", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("pidgeot", "blink")}
        val flyQuirk1 = quirk { bedrockStateful("pidgeot", "air_fly_quirk") }
        val flyQuirk2 = quirk { bedrockStateful("pidgeot", "air_fly_quirk2") }

//        sleep = registerPose(
//            poseName = "sleeping",
//            transformedParts = arrayOf(
//                wingClosedLeft.createTransformation().withVisibility(visibility = true),
//                wingClosedRight.createTransformation().withVisibility(visibility = true),
//                wingOpenLeft.createTransformation().withVisibility(visibility = false),
//                wingOpenRight.createTransformation().withVisibility(visibility = false)
//            ),
//            poseType = PoseType.SLEEP,
//            animations = arrayOf(bedrock("pidgeot", "sleep"))
//        )

        stand = registerPose(
            poseName = "stand",
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = true),
                wingClosedRight.createTransformation().withVisibility(visibility = true),
                wingOpenLeft.createTransformation().withVisibility(visibility = false),
                wingOpenRight.createTransformation().withVisibility(visibility = false)
            ),
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER - PoseType.FLOAT + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeot", "ground_idle_PLACEHOLDER"),
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = true),
                wingClosedRight.createTransformation().withVisibility(visibility = true),
                wingOpenLeft.createTransformation().withVisibility(visibility = false),
                wingOpenRight.createTransformation().withVisibility(visibility = false)
            ),
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY - PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeot", "ground_idle_PLACEHOLDER"),
                BipedWalkAnimation(this)
            )
        )

        hover = registerPose(
            poseName = "floating",
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = false),
                wingClosedRight.createTransformation().withVisibility(visibility = false),
                wingOpenLeft.createTransformation().withVisibility(visibility = true),
                wingOpenRight.createTransformation().withVisibility(visibility = true)
            ),
            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeot", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "flying",
            transformedParts = arrayOf(
                wingClosedLeft.createTransformation().withVisibility(visibility = false),
                wingClosedRight.createTransformation().withVisibility(visibility = false),
                wingOpenLeft.createTransformation().withVisibility(visibility = true),
                wingOpenRight.createTransformation().withVisibility(visibility = true)
            ),
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            quirks = arrayOf(blink, flyQuirk1, flyQuirk2),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("pidgeot", "air_fly")
            )
        )
    }
}