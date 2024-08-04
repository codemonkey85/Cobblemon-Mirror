/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class FarfetchdModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("farfetchd")
    override val head = getPart("neck")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.6F
    override var portraitTranslation = Vec3(-0.2, -1.0, 0.0)

    override var profileScale = 1.1F
    override var profileTranslation = Vec3(-0.1, 0.1, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose

    override val cryAnimation = CryProvider { bedrockStateful("farfetchd", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("farfetchd", "blink") }
        val leakflipidle = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("farfetchd", "quirk_leakflip_idle") }
        val leakflipwalk = quirk(secondsBetweenOccurrences = 30F to 120F) { bedrockStateful("farfetchd", "quirk_leakflip_walk") }
        val wink = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("farfetchd", "quirk_wink") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("farfetchd", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + UI_POSES - PoseType.HOVER,
            transformTicks = 10,
            quirks = arrayOf(blink, leakflipidle, wink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("farfetchd", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            transformTicks = 5,
            quirks = arrayOf(blink, leakflipwalk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("farfetchd", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("farfetchd", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("farfetchd", "air_fly")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("farfetchd", "faint") else null
}