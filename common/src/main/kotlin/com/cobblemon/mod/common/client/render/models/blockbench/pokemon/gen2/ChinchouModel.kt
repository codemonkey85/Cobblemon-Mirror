/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ChinchouModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("chinchou")

    override var portraitScale = 1.75F
    override var portraitTranslation = Vec3(-0.3, -0.8, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(-0.05, 0.45, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var floating: CobblemonPose
    lateinit var swimming: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var watersleep: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var waterbattleidle: CobblemonPose

//    override val cryAnimation = CryProvider { bedrockStateful("chinchou", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("chinchou", "blink")}
        val idleQuirk = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("chinchou", "ground_quirk")}
        val waterQuirk = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("chinchou", "water_idle_quirk")}

        sleep = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            animations = arrayOf(bedrock("chinchou", "sleep"))
        )

        watersleep = registerPose(
            poseName = "water_sleeping",
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            animations = arrayOf(bedrock("chinchou", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            transformTicks = 10,
            condition = { !it.isBattling && !it.isInWater && !it.isUnderWater},
            quirks = arrayOf(blink, idleQuirk),
            animations = arrayOf(
                bedrock("chinchou", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformTicks = 10,
            poseType = PoseType.WALK,
            condition = { !it.isInWater && !it.isUnderWater},
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chinchou", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            condition = { it.isInWater },
            quirks = arrayOf(blink, waterQuirk),
            animations = arrayOf(
                bedrock("chinchou", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
            condition = { it.isInWater },
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("chinchou", "water_swim"),
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && !it.isInWater },
            animations = arrayOf(
                bedrock("chinchou", "battle_idle")
            )
        )

        waterbattleidle = registerPose(
            poseName = "water_battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && it.isInWater },
            animations = arrayOf(
                bedrock("chinchou", "water_battle_idle")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, battleidle, sleep)) bedrockStateful("chinchou", "faint") else if (state.isPosedIn( waterbattleidle, watersleep, floating, swimming )) bedrockStateful("chinchou", "water_faint") else null
}