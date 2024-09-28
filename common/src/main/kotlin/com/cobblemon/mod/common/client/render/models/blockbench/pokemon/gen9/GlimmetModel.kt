/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GlimmetModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("glimmet")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.5, 1.3, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose
    lateinit var shoulderLeft: Pose
    lateinit var shoulderRight: Pose

    val shoulderOffset = 8

    override val cryAnimation = CryProvider { bedrockStateful("glimmet", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("glimmet", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("glimmet", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("glimmet", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("glimmet", "ground_walk")
            )
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("glimmet", "ground_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, shoulderOffset)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("glimmet", "ground_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -shoulderOffset)
                )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk)) bedrockStateful("glimmet", "faint") else null
}