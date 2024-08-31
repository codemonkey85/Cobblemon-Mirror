/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CopperajahModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("copperajah")

    override var portraitScale = 0.6F
    override var portraitTranslation = Vec3(-0.67, 1.62, 0.0)

    override var profileScale = 0.30F
    override var profileTranslation = Vec3(-0.02, 1.2, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battle_idle: CobblemonPose

    override fun registerPoses() {
        animations["physical"] = "q.bedrock_primary('copperajah', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["special"] = "q.bedrock_primary('copperajah', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["status"] = "q.bedrock_primary('copperajah', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["recoil"] = "q.bedrock_stateful('copperajah', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('copperajah', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('copperajah', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("copperajah", "blink") }
        val quirk = quirk(30F to 60F) { bedrockStateful("copperajah", "quirk_idle") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("copperajah", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, quirk),
            condition = { !it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("copperajah", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink, quirk),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("copperajah", "ground_walk")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, quirk),
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("copperajah", "battle_idle")
            )
        )
    }
}