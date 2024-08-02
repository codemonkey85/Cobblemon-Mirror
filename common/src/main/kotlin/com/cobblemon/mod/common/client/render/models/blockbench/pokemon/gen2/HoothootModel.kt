/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class HoothootModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("hoothoot")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3(-0.25, -0.9, 0.0)
    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.3, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("hoothoot", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("hoothoot", "blink")}
        val quirk = quirk { bedrockStateful("hoothoot", "quirk_foot")}

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("hoothoot", "sleep"))
        )
        standing = registerPose(
            poseName = "stand",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER - PoseType.FLOAT + PoseType.UI_POSES,
            condition = { !it.isBattling },
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                bedrock("hoothoot", "ground_idle")
            )
        )
        hover = registerPose(
            poseName = "hover",
            poseTypes = setOf(PoseType.HOVER, PoseType.FLOAT),
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("hoothoot", "air_idle")
            )
        )
        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("hoothoot", "air_fly")
            )
        )
        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY - PoseType.SWIM,
            transformTicks = 5,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("hoothoot", "ground_walk")
            )
        )
        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("hoothoot", "battle_idle")
            )
        )
    }
}