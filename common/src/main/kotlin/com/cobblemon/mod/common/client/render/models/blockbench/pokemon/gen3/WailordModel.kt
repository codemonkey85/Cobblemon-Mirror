/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class WailordModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("wailord")
    override var portraitScale = 0.45F
    override var portraitTranslation = Vec3(-0.38, 0.8, 6.69)

    override var profileScale = 0.25F
    override var profileTranslation = Vec3(0.0, 1.2, -10.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    val offsetY = 0.0
    override fun registerPoses() {
        val blink = quirk { bedrockStateful("wailord", "blink")}

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("wailord", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES - PoseType.FLOAT,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wailord", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wailord", "ground_idle"),
                bedrock("wailord", "ground_walk")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wailord", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("wailord", "water_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("wailord", "battle_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep, battleidle)) bedrockStateful("wailord", "faint") else
        if (state.isPosedIn(floating, swimming)) bedrockStateful("wailord", "faint_water")
        else null
}