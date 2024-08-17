/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class RattataModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("rattata")
    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")
    override val head = getPart("head")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3(-0.2, -1.4, 0.0)

    override var profileScale = 1.1F
    override var profileTranslation = Vec3(0.0, 0.1, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("rattata", "cry") }

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("rattata", "sleep"))
        )

        val blink = quirk { bedrockStateful("rattata", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.PROFILE, PoseType.STAND, PoseType.FLOAT, PoseType.PORTRAIT, PoseType.SHOULDER_LEFT, PoseType.SHOULDER_RIGHT),
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rattata", "ground_idle")
            )
        )

        walk = registerPose(
            poseType = PoseType.WALK,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("rattata", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("rattata", "faint")
}
