/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7


import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CutieflyModel(root: ModelPart) : PokemonPosableModel(root){
    override val rootPart = root.registerChildWithAllChildren("cutiefly")

    override var portraitScale = 2.4F
    override var portraitTranslation = Vec3(-0.3, 1.5, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 1.2, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    val shoulderOffset = 12.5

    override fun registerPoses() {

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("cutiefly", "sleep"))
        )

       standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            animations = arrayOf(
                bedrock("cutiefly", "ground_idle")
            ),
           transformedParts = arrayOf(rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -12F))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            animations = arrayOf(
                bedrock("cutiefly", "ground_walk"),
            ),
            transformedParts = arrayOf(rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -12F))
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                animations = arrayOf(
                        bedrock("cutiefly", "ground_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(shoulderOffset, -4, 0)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                animations = arrayOf(
                        bedrock("cutiefly", "ground_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(-shoulderOffset, -4, 0)
                )
        )
    }
        override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("cutiefly", "faint") else null
}