/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class VenipedeModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("venipede")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.5, -1.4, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("venipede", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("venipede", "blink") }
        val twitch = quirk { bedrockStateful("venipede", "twitch") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, twitch),
            animations = arrayOf(
                bedrock("venipede", "ground_idle")
            )
        )

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(twitch),
            animations = arrayOf(
                bedrock("venipede", "sleep"),
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink, twitch),
            animations = arrayOf(
                bedrock("venipede", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//            pokemonEntity: PokemonEntity,
//            state: PosableState<PokemonEntity>
//    ) = if (state.isNotPosedIn(sleep)) bedrockStateful("venipede", "faint") else null
}