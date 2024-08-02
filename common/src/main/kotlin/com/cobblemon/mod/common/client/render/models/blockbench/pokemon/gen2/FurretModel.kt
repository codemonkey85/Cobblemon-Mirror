/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class FurretModel (root: ModelPart) : PokemonPosableModel(root), QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("furret")

    override val foreLeftLeg = getPart("arm_left")
    override val foreRightLeg = getPart("arm_right")
    override val hindLeftLeg = getPart("leg_left")
    override val hindRightLeg = getPart("leg_right")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3(-0.6, 0.2, 0.0)

    override var profileScale = 0.82F
    override var profileTranslation = Vec3(0.0, 0.54, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("furret", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("furret", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                QuadrupedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F),
                bedrock("furret", "pose")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("furret", "faint") else null
}