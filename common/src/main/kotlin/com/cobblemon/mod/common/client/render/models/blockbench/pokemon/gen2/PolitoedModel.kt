/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PolitoedModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("politoed")
    override val head = getPart("head")

    override val leftLeg = getPart("leftleg")
    override val rightLeg = getPart("rightleg")

    override var portraitScale = 2.4F
    override var portraitTranslation = Vec3(-0.04, -0.55, 0.0)

    override var profileScale = 1.1F
    override var profileTranslation = Vec3(0.0, 0.1, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseType = PoseType.WALK,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "ground_idle")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseTypes = setOf(PoseType.SWIM, PoseType.FLY),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "water_swim")
            )
        )
    }

    //    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("politoed", "faint") else null
}