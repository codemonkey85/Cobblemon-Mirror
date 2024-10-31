/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PoliwrathModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("poliwrath")

    override var portraitScale = 1.3F
    override var portraitTranslation = Vec3(-0.1, 0.36, 0.0)

    override var profileScale = 0.85F
    override var profileTranslation = Vec3(0.0, 0.42, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("poliwrath", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("poliwrath", "ground_idle")
            )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("poliwrath", "sleep"))
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("poliwrath", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("poliwrath", "water_swim")
            )
        )
    }


//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("poliwrath", "faint") else null
}