/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class BarboachModel (root: ModelPart) : PosableModel() {
    override val rootPart = root.registerChildWithAllChildren("barboach")

    override val portraitScale = 2.0F
    override val portraitTranslation = Vec3d(-0.55, -1.2, 0.0)

    override val profileScale = 0.8F
    override val profileTranslation = Vec3d(0.0, 0.4, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose
    lateinit var watersleep: Pose
    lateinit var battleidle: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("barboach", "blink") }

        watersleep = registerPose(
            poseType = PoseType.SLEEP,
            condition = { it.entity?.isTouchingWater == true },
            idleAnimations = arrayOf(bedrock("barboach", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES - PoseType.FLOAT,
            condition = { (it.entity as? PokemonEntity)?.isBattling == false },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("barboach", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("barboach", "ground_idle")
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("barboach", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("barboach", "water_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { (it.entity as? PokemonEntity)?.isBattling == true },
            idleAnimations = arrayOf(
                bedrock("barboach", "battle_idle")
            )
        )
    }
}