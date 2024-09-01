/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class NaclstackModel(root: ModelPart) : PokemonPosableModel(root), QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("naclstack")
    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 4.0F
    override var portraitTranslation = Vec3(-0.61, -3.0, 0.0)

    override var profileScale = 1.05F
    override var profileTranslation = Vec3(0.1, 0.1, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("naclstack", "cry") }

    override fun registerPoses() {
        standing = registerPose(
                poseName = "standing",
                poseTypes = STATIONARY_POSES + UI_POSES,
                animations = arrayOf(
                        bedrock("naclstack", "ground_idle")
                )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("naclstack", "sleep"))
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = MOVING_POSES,
                animations = arrayOf(
                        bedrock("naclstack", "ground_idle"),
                        bedrock("naclstack", "ground_walk")
                )
        )
    }
}