/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MeowscaradaModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("meowscarada")
    override val head = getPart("head")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-0.3, 3.0, 0.0)

    override var profileScale = 0.46F
    override var profileTranslation = Vec3(0.0, 1.1, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("meowscarada", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("meowscarada", "blink") }
        val sleep1 = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("meowscarada", "sleep_quirk") }
        val sleep2 = quirk(secondsBetweenOccurrences = 30F to 120F) { bedrockStateful("meowscarada", "sleep_quirk2") }
        val sleep3 = quirk(secondsBetweenOccurrences = 20F to 60F) { bedrockStateful("meowscarada", "sleep_quirk3") }
        val sleep4 = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("meowscarada", "sleep_quirk4") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(blink, sleep1, sleep2, sleep3, sleep4),
            animations = arrayOf(bedrock("meowscarada", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("meowscarada", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("meowscarada", "ground_walk")
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
                bedrock("meowscarada", "battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("meowscarada", "faint") else null
}