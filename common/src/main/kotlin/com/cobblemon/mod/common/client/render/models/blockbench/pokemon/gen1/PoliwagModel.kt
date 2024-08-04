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
import com.cobblemon.mod.common.entity.PoseType.Companion.STANDING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class PoliwagModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("poliwag")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(0.0, -0.7, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.38, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("poliwag", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = STANDING_POSES + UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("poliwag", "ground_idle")
            )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("poliwag", "sleep"))
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("poliwag", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("poliwag", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("poliwag", "faint") else null
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
}