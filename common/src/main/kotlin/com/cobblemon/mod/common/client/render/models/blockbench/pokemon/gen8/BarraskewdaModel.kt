/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class BarraskewdaModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("barraskewda")

    override var portraitScale = 2.8F
    override var portraitTranslation = Vec3(-0.7, -2.8, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose
    lateinit var sleep: Pose
    lateinit var watersleep: Pose
    lateinit var battleidle: Pose
    lateinit var waterbattleidle: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("barraskewda", "blink")}
        sleep = registerPose(
            poseName = "sleeping",
            transformTicks = 10,
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            animations = arrayOf(bedrock("barraskewda", "sleep"))
        )

        watersleep = registerPose(
            poseName = "water_sleeping",
            transformTicks = 10,
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            animations = arrayOf(bedrock("barraskewda", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            condition = { !it.isBattling },
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("barraskewda", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("barraskewda", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseType = PoseType.FLOAT,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("barraskewda", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("barraskewda", "water_swim"),
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && !it.isInWater },
            animations = arrayOf(
                bedrock("barraskewda", "battle_idle")
            )
        )

        waterbattleidle = registerPose(
            poseName = "water_battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && it.isInWater },
            animations = arrayOf(
                bedrock("barraskewda", "water_battle_idle")
            )
        )
    }
}