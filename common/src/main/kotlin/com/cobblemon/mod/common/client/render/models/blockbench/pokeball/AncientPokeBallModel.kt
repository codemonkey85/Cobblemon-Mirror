/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokeball

import com.cobblemon.mod.common.client.render.models.blockbench.frame.PokeBallFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import net.minecraft.client.model.geom.ModelPart

class AncientPokeBallModel(root: ModelPart) : PokeBallModel(root), PokeBallFrame {
    override val rootPart = root.registerChildWithAllChildren("poke_ball")
    override val base = getPart("bottom")
    override val lid = getPart("lid")

    override lateinit var shut: Pose
    override lateinit var open: Pose
    override lateinit var midair: Pose

    override fun registerPoses() {
        midair = registerPose(
            poseName = "flying",
            poseTypes = setOf(PoseType.NONE),
            condition = { (it.getEntity() as? EmptyPokeBallEntity)?.captureState == EmptyPokeBallEntity.CaptureState.NOT },
            transformTicks = 0,
            animations = arrayOf(bedrock("ancient_poke_ball", "throw"))
        )

        shut = registerPose(
            poseName = "shut",
            poseTypes = setOf(PoseType.NONE, PoseType.PORTRAIT),
            animations = arrayOf(bedrock("ancient_poke_ball", "shut_idle")),
            transformTicks = 0
        )

        open = registerPose(
            poseName = "open",
            poseTypes = setOf(PoseType.NONE, PoseType.PORTRAIT),
            animations = arrayOf(bedrock("ancient_poke_ball", "open_idle")),
            transformTicks = 0
        )

        shut.transitions[open.poseName] = { _, _ -> bedrockStateful("ancient_poke_ball", "open") }
        open.transitions[shut.poseName] = { _, _ -> bedrockStateful("ancient_poke_ball", "shut") }
        midair.transitions[open.poseName] = shut.transitions[open.poseName]!!
    }
}