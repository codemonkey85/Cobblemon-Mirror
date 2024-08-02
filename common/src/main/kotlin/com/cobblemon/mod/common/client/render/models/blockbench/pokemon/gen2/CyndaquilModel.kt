/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CyndaquilModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("cyndaquil")
    override val head = getPart("head")

    override var portraitScale = 1.4F
    override var portraitTranslation = Vec3(-0.26, 0.0, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { if (it.isBattling) bedrockStateful("cyndaquil", "battle_cry") else bedrockStateful("cyndaquil", "cry") }

    override fun registerPoses() {
//        val sneeze = quirk { bedrockStateful("cyndaquil", "sneeze_quirk") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("cyndaquil", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
//            quirks = arrayOf(sneeze),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("cyndaquil", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
//            quirks = arrayOf(sneeze),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("cyndaquil", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
//            quirks = arrayOf(sneeze),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(minPitch = 0F),
                bedrock("cyndaquil", "battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("cyndaquil", "faint") else null
}