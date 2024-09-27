/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokeball

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.frame.PokeBallFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.model.geom.ModelPart

open class RaftPlatformModel(root: ModelPart) : PosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("water_platform_m")
    override var isForLivingEntityRenderer = false

    open lateinit var idle: Pose
//    open lateinit var open: Pose
//    open lateinit var midair: Pose

    override fun registerPoses() {
        idle = registerPose(
                poseName = "water_platform_m",
                poseTypes = setOf(PoseType.NONE),
                condition = { (it.getEntity() as? EmptyPokeBallEntity)?.captureState == EmptyPokeBallEntity.CaptureState.NOT },
                transformTicks = 0,
                animations = arrayOf()
        )

//        shut = registerPose(
//            poseName = "shut",
//            poseTypes = setOf(PoseType.NONE, PoseType.PORTRAIT),
//            animations = arrayOf(bedrock("poke_ball", "shut_idle")),
//            transformTicks = 0
//        )
//
//        open = registerPose(
//            poseName = "open",
//            poseTypes = setOf(PoseType.NONE, PoseType.PORTRAIT),
//            animations = arrayOf(bedrock("poke_ball", "open_idle")),
//            transformTicks = 0
//        )

//        shut.transitions[open.poseName] = { _, _ -> bedrockStateful("poke_ball", "open") }
//        open.transitions[shut.poseName] = { _, _ -> bedrockStateful("poke_ball", "shut") }
//        midair.transitions[open.poseName] = shut.transitions[open.poseName]!!
    }
}