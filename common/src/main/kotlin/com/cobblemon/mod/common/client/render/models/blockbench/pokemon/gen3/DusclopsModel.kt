/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DusclopsModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("dusclops")

    override var portraitScale = 1.6F
    override var portraitTranslation = Vec3(-0.2, 1.1, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var battleidle: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dusclops", "blink") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("dusclops", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dusclops", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dusclops", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = PoseType.UI_POSES + PoseType.HOVER + PoseType.FLOAT,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dusclops", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM, PoseType.WALK),
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dusclops", "air_fly")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("dusclops", "battle_idle")
            )

        )
    }

    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(hover, fly, sleep, standing, walk, battleidle)) bedrockStateful("dusclops", "faint") else null
}