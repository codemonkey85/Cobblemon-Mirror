/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GimmighoulRoamingModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("gimmighoul_roaming")
    override val head = getPart("head")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(0.0, -1.3, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.76, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleIdle: CobblemonPose
    lateinit var sleep: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("gimmighoul_roaming", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("gimmighoul_roaming", "blink") }
        val quirk = quirk(secondsBetweenOccurrences = 30F to 120F) { bedrockStateful("gimmighoul_roaming", "idle_quirk") }
        val sleepQuirk = quirk (secondsBetweenOccurrences = 30F to 60F) { bedrockStateful("gimmighoul_roaming", "sleep_quirk") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            quirks = arrayOf(sleepQuirk),
            animations = arrayOf(bedrock("gimmighoul_roaming", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink,quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_roaming", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_roaming", "ground_walk")
            )
        )

        battleIdle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_roaming", "battle_idle")
            )
        )
    }

    override fun getFaintAnimation(state: PosableState) = bedrockStateful("gimmighoul_roaming", "faint")
}