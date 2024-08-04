/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class HaunterModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("haunter")

    override var portraitScale = 1.3F
    override var portraitTranslation = Vec3(-0.25, -0.1, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(-0.1, 0.75, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    val offsetY = -3.0

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("haunter", "blink")}

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("haunter", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("haunter", "ground_idle")
                //bedrock("haunter", "ground_walk")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("haunter", "faint") else null
}