/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class OinkologneFemaleModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("oinkologne_female")
    override val head = getPart("head")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.65, -0.5, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.35, 0.0)

    //    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("oinkologne_female", "cry") }

    override fun registerPoses() {
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            animations = arrayOf(bedrock("oinkologne_female", "sleep"))
//        )

        val blink = quirk { bedrockStateful("oinkologne_female", "blink") }

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("oinkologne_female", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 5,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("oinkologne_female", "ground_idle"),
                        QuadrupedWalkAnimation(this, periodMultiplier = 0.75F, amplitudeMultiplier = 1F)
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isNotPosedIn(sleep)) bedrockStateful("oinkologne_female", "faint") else null
}