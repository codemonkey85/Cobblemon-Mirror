/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.SingleBoneLookAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ExeggutorAlolanModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("exeggutor")
    override val head = getPart("head")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    val head2 = object : HeadedFrame {
        override val rootPart = this@ExeggutorAlolanModel.rootPart
        override val head: ModelPart = getPart("head2")
    }
    val head3 = object : HeadedFrame {
        override val rootPart = this@ExeggutorAlolanModel.rootPart
        override val head: ModelPart = getPart("head3")
    }

    override var portraitScale = 1.0F
    override var portraitTranslation = Vec3(-1.4, 16.55, 0.0)

    override var profileScale = 0.45F
    override var profileTranslation = Vec3(0.0, 1.0, -30.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override fun registerPoses() {
        val blink1 = quirk { bedrockStateful("exeggutor_alolan", "blink") }
        val blink2 = quirk { bedrockStateful("exeggutor_alolan", "blink2") }
        val blink3 = quirk { bedrockStateful("exeggutor_alolan", "blink3") }
        val blink4 = quirk { bedrockStateful("exeggutor_alolan", "blink4") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("exeggutor_alolan", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink1, blink2, blink3, blink4),
            condition = { !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
                SingleBoneLookAnimation(head3, false, false, disableX = false, disableY = false),
                bedrock("exeggutor_alolan", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink1, blink2, blink3, blink4),
            animations = arrayOf(
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
                SingleBoneLookAnimation(head3, false, false, disableX = false, disableY = false),
                bedrock("exeggutor_alolan", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3, blink4),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                SingleBoneLookAnimation(head2, false, false, disableX = false, disableY = false),
                SingleBoneLookAnimation(head3, false, false, disableX = false, disableY = false),
                bedrock("exeggutor_alolan", "battle_idle")
            )

        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("exeggutor_alolan", "faint") else null
}