/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BimanualSwingAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SliggooHisuianModel (root: ModelPart) : PokemonPosableModel(root), BimanualFrame, HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("sliggoo_hisuian")
    override val head = getPart("head")

    override val leftArm = getPart("arm_right")
    override val rightArm = getPart("arm_left")

    override var portraitScale = 1.72F
    override var portraitTranslation = Vec3(-0.4, 0.8, 0.0)

    override var profileScale = 0.57F
    override var profileTranslation = Vec3(0.07, 0.97, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("sliggoo_hisuian", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("sliggoo_hisuian", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sliggoo_hisuian", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sliggoo_hisuian", "ground_idle"),
                BimanualSwingAnimation(this, 0.4F, 1F)
            )
        )
    }
}