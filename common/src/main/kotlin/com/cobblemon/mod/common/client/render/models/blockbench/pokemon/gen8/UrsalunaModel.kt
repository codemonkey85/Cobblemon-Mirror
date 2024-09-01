/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class UrsalunaModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("ursaluna")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("shoulder_left")
    override val foreRightLeg = getPart("shoulder_right")
    override val hindLeftLeg = getPart("thigh_left")
    override val hindRightLeg = getPart("thigh_right")

    override var portraitScale = 1.2F
    override var portraitTranslation = Vec3(-1.1, 0.4, 0.0)

    override var profileScale = 0.45F
    override var profileTranslation = Vec3(0.0, 0.9, 0.0)

    //    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("ursaluna", "cry") }

    override fun registerPoses() {
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            animations = arrayOf(bedrock("ursaluna", "sleep"))
//        )

        val blink = quirk { bedrockStateful("ursaluna", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ursaluna", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 5,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ursaluna", "ground_idle"),
                QuadrupedWalkAnimation(this, periodMultiplier = 0.75F, amplitudeMultiplier = 1F)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isNotPosedIn(sleep)) bedrockStateful("ursaluna", "faint") else null
}