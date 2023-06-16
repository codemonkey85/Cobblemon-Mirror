/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class RibombeeModel(root: ModelPart) : PokemonPoseableModel(){
    override val rootPart = root.registerChildWithAllChildren("ribombee")

    override val portraitScale = 2.2F
    override val portraitTranslation = Vec3d(-0.4, 2.7, 0.0)

    override val profileScale = 0.5F
    override val profileTranslation = Vec3d(0.1, 1.4, 0.0)

    lateinit var idle: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose

    override fun registerPoses() {

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("ribombee", "sleep"))
        )

       idle = registerPose(
            poseName = "idle",
            poseTypes = STATIONARY_POSES + UI_POSES,
            idleAnimations = arrayOf(
                bedrock("ribombee", "ground_idle")
            ),
           transformedParts = arrayOf(rootPart.asTransformed().addPosition(Y_AXIS, -12F))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            idleAnimations = arrayOf(
                bedrock("ribombee", "ground_walk")
            ),
            transformedParts = arrayOf(rootPart.asTransformed().addPosition(Y_AXIS, -12F))
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(idle, walk)) bedrockStateful("ribombee", "faint") else null
}