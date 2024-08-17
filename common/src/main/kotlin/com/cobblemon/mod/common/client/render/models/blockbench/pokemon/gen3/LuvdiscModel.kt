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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class LuvdiscModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("luvdisc")


    override var portraitScale = 6.0F
    override var portraitTranslation = Vec3(0.15, -6.5, 0.0)

    override var profileScale = 2.0F
    override var profileTranslation = Vec3(0.0, -1.3, 0.0)

    lateinit var standing: Pose
    lateinit var sleep: Pose
    lateinit var walk: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("luvdisc", "blink")}
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("luvdisc", "sleep"))
        )
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("luvdisc", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("luvdisc", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("luvdisc", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("luvdisc", "water_swim")
            )
        )
    }
        override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("luvdisc", "ground_faint") else null

}