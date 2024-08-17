/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class TatsugiriModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("tatsugiri")
    override val head = getPart("head")

    override var portraitScale = 5.0F
    override var portraitTranslation = Vec3(0.0, -4.5, 0.0)

    override var profileScale = 1.4F
    override var profileTranslation = Vec3(0.0, -0.3, 0.0)

    lateinit var swimming: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("tatsugiri", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("tatsugiri", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("tatsugiri", "ground_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseTypes = PoseType.SWIMMING_POSES,
            animations = arrayOf(
                bedrock("tatsugiri", "water_idle")
            )
        )
    }
}