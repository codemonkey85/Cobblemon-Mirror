/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class RaichuModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("raichu")
    override val head = getPart("head")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3(-0.2, -0.1, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.75, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("raichu", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("raichu", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("raichu", "ground_idle")
            )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("raichu", "sleep"))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("raichu", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("raichu", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk, battleidle, sleep)) bedrockStateful("raichu", "faint") else null
}