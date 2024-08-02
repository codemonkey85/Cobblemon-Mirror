/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.SingleBoneLookAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DodrioModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dodrio")
    override val head = getPart("head4")

    val lefthead = object : HeadedFrame {
        override val rootPart = this@DodrioModel.rootPart
        override val head: ModelPart = getPart("head3")
    }
    val righthead = object : HeadedFrame {
        override val rootPart = this@DodrioModel.rootPart
        override val head: ModelPart = getPart("head2")
    }

    override var portraitScale = 1.5F
    override var portraitTranslation = Vec3(-0.15, 0.9, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.6, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("dodrio", "cry") }

    override fun registerPoses() {
        val blink1 = quirk { bedrockStateful("dodrio", "blink1") }
        val blink2 = quirk { bedrockStateful("dodrio", "blink2") }
        val blink3 = quirk { bedrockStateful("dodrio", "blink3") }
        val bite1 = quirk(secondsBetweenOccurrences = 5F to 20F) { bedrockStateful("dodrio", "bite_quirk1") }
        val bite2 = quirk(secondsBetweenOccurrences = 5F to 20F) { bedrockStateful("dodrio", "bite_quirk2") }
        val bite3 = quirk(secondsBetweenOccurrences = 5F to 20F) { bedrockStateful("dodrio", "bite_quirk3") }


        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("dodrio", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink1, blink2, blink3),
            animations = arrayOf(
                singleBoneLook(pitchMultiplier = 1F, yawMultiplier = 0.4F),
                SingleBoneLookAnimation(lefthead, false, false, false, false, 1F, 1.5F, 45F, -45F, 45F, 10F),
                SingleBoneLookAnimation(righthead, false, false, false, false, 1F, 1.5F, 45F, -45F, 10F, -45F),
                bedrock("dodrio", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3),
            animations = arrayOf(
                bedrock("dodrio", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3, bite1, bite2, bite3),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("dodrio", "battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("dodrio", "faint") else null
}