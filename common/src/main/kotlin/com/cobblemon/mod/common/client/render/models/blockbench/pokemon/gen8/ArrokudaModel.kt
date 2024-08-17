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

class ArrokudaModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("arrokuda")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3(-0.7, -3.2, 0.0)

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
        val blink = quirk { bedrockStateful("arrokuda", "blink")}
        val flop = quirk { bedrockStateful("arrokuda", "ground_quirk")}

        sleep = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            condition = { !it.isInWater },
            animations = arrayOf(bedrock("arrokuda", "sleep"))
        )

        watersleep = registerPose(
            poseName = "water_sleeping",
            poseType = PoseType.SLEEP,
            condition = { it.isInWater },
            animations = arrayOf(bedrock("arrokuda", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink, flop),
            animations = arrayOf(
                bedrock("arrokuda", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformTicks = 10,
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("arrokuda", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            animations = arrayOf(
                bedrock("arrokuda", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
            poseType = PoseType.SWIM,
            animations = arrayOf(
                bedrock("arrokuda", "water_swim"),
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, flop),
            condition = { it.isBattling && !it.isInWater },
            animations = arrayOf(
                bedrock("arrokuda", "battle_idle")
            )
        )

        waterbattleidle = registerPose(
            poseName = "water_battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && it.isInWater },
            animations = arrayOf(
                bedrock("arrokuda", "water_battle_idle")
            )
        )
    }
}