/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MewModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("mew")
    override val head = getPart("head")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3(-0.15, 0.4, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.65, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var shoulderLeft: Pose
    lateinit var shoulderRight: Pose

    val shoulderOffset = 1.5

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mew", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mew", "ground_walk")
            )
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                //quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("mew", "ground_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, shoulderOffset)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                //quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("mew", "ground_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -shoulderOffset)
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("mew", "faint") else null
}