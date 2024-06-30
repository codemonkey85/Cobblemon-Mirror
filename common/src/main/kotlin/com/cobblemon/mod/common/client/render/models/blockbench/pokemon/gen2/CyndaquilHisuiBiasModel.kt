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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class CyndaquilHisuiBiasModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("cyndaquil_hisui_bias")
    override val head = getPart("head")

    override var portraitScale = 1.4F
    override var portraitTranslation = Vec3d(-0.26, 0.0, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3d(0.0, 0.8, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { if (it.isBattling) bedrockStateful("cyndaquil_hisui_bias", "battle_cry") else bedrockStateful("cyndaquil_hisui_bias", "cry") }

    override fun registerPoses() {
//        val sneeze = quirk { bedrockStateful("cyndaquil_hisui_bias", "sneeze_quirk") }

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("cyndaquil_hisui_bias", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                condition = { !it.isBattling },
//            quirks = arrayOf(sneeze),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("cyndaquil_hisui_bias", "ground_idle")
                )
        )

        walking = registerPose(
                poseName = "walking",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
//            quirks = arrayOf(sneeze),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("cyndaquil_hisui_bias", "ground_walk")
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
                        bedrock("cyndaquil_hisui_bias", "battle_idle")
                )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("cyndaquil_hisui_bias", "faint") else null
}