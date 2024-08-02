/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class IronleavesModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("ironleaves")
    override val head = getPart("head")
    val neck_sword_left = getPart("neck_sword_left")
    val neck_sword_right = getPart("neck_sword_right")
    val sword_head = getPart("sword_head")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3(-0.9, 2.2, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("ironleaves", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                neck_sword_left.createTransformation().withVisibility(visibility = false),
                neck_sword_right.createTransformation().withVisibility(visibility = false),
                sword_head.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ironleaves", "ground_idle")
            )
        )

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformedParts = arrayOf(
                neck_sword_left.createTransformation().withVisibility(visibility = false),
                neck_sword_right.createTransformation().withVisibility(visibility = false),
                sword_head.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(bedrock("ironleaves", "sleep"))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                neck_sword_left.createTransformation().withVisibility(visibility = false),
                neck_sword_right.createTransformation().withVisibility(visibility = false),
                sword_head.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ironleaves", "ground_walk")
            )
        )
    }
}