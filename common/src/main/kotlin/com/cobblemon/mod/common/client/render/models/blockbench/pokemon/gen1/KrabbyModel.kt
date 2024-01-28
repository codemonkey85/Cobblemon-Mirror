/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class KrabbyModel(root: ModelPart) : PosableModel() {
    override val rootPart = root.registerChildWithAllChildren("krabby")

    override val portraitScale = 2.5F
    override val portraitTranslation = Vec3d(-0.15, -1.8, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.2, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var battleidle: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("krabby", "blink")}
        val snipLeft = quirk { bedrockStateful("krabby", "snip_left")}
        val snipRight = quirk { bedrockStateful("krabby", "snip_right")}

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            condition = { (it.entity as? PokemonEntity)?.isBattling == false },
            quirks = arrayOf(blink, snipLeft, snipRight),
            idleAnimations = arrayOf(
                bedrock("krabby", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink, snipLeft, snipRight),
            idleAnimations = arrayOf(
                bedrock("krabby", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, snipLeft, snipRight),
            condition = { (it.entity as? PokemonEntity)?.isBattling == true },
            idleAnimations = arrayOf(
                bedrock("krabby", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("krabby", "faint") else null
}