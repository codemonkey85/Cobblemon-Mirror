/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CacneaModel (root: ModelPart) : PokemonPosableModel(root), BimanualFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("cacnea")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.2, -1.0, 0.0)

    override var profileScale = 0.85F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("cacnea", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("cacnea", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("cacnea", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            transformTicks = 10,
            animations = arrayOf(
                bedrock("cacnea", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                bedrock("cacnea", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("cacnea", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("cacnea", "faint") else null
}