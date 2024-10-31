/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MachokeModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("machoke")
    override val head = getPart("head")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-0.1, 1.0, 0.0)

    override var profileScale = 0.71F
    override var profileTranslation = Vec3(0.0, 0.69, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("machoke", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("machoke", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("machoke", "ground_walk")
            )
        )
    }

    /*override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk)) bedrockStateful("machoke", "faint") else null
     */
}