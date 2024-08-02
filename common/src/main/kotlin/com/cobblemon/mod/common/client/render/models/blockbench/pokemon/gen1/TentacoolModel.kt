/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class TentacoolModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("tentacool")

    override var portraitScale = 1.2F
    override var portraitTranslation = Vec3(0.1, 1.1, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var watersleep: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("tentacool", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("tentacool", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("tentacool", "ground_idle")
                //bedrock("tentacool", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("tentacool", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("tentacool", "water_swim")
            )
        )

        sleep = registerPose(
                poseName = "sleep",
                poseType = PoseType.SLEEP,
                condition = { !it.isInWater },
                animations = arrayOf(bedrock("tentacool", "sleep"))
        )

        watersleep = registerPose(
                poseName = "watersleep",
                poseType = PoseType.SLEEP,
                condition = { it.isInWater },
                animations = arrayOf(bedrock("tentacool", "water_sleep"))
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("tentacool", "faint") else null
}