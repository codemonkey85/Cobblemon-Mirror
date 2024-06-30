/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class QuilavaHisuiBiasModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("quilava_hisui_bias")
    override val head = getPart("head")

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3d(-0.65, -0.6, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3d(0.0, 0.65, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { if (it.isBattling) bedrockStateful("quilava_hisui_bias", "battle_cry") else PrimaryAnimation(bedrockStateful("quilava_hisui_bias", "cry")) }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("quilava_hisui_bias", "blink") }

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                animations = arrayOf(bedrock("quilava_hisui_bias", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                condition = { !it.isBattling },
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("quilava_hisui_bias", "fire_idle"),
                        bedrock("quilava_hisui_bias", "ground_idle")
                )
        )

        walking = registerPose(
                poseName = "walking",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("quilava_hisui_bias", "fire_idle"),
                        bedrock("quilava_hisui_bias", "ground_walk")
                )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling },
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("quilava_hisui_bias", "fire_idle"),
                        bedrock("quilava_hisui_bias", "battle_idle")
                )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("quilava_hisui_bias", "faint") else null
}