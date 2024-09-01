/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class LuxioModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("luxio")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("front_leg_left")
    override val foreRightLeg = getPart("front_leg_right")
    override val hindLeftLeg = getPart("back_leg_left")
    override val hindRightLeg = getPart("back_leg_right")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.6, -0.25, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose

    override fun registerPoses() {
//        val blink = quirk { bedrockStateful("luxio", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("luxio", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
//            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("luxio", "idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
//            quirks = arrayOf(blink),
            animations = arrayOf(
                QuadrupedWalkAnimation(this, periodMultiplier = 1.1F),
                singleBoneLook(),
                bedrock("luxio", "idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("luxio", "faint") else null
}