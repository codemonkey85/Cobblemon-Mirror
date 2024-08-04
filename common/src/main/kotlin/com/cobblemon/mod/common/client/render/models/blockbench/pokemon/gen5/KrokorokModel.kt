/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class KrokorokModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("krokorok")

    override var portraitScale = 1.86F
    override var portraitTranslation = Vec3(-0.43, 0.98, 0.0)

    override var profileScale = 0.55F
    override var profileTranslation = Vec3(0.05, 0.93, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override fun registerPoses() {
//        animations["physical"] = "q.bedrock_primary('krokorok', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["special"] = "q.bedrock_primary('krokorok', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["status"] = "q.bedrock_primary('krokorok', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["recoil"] = "q.bedrock_stateful('krokorok', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('krokorok', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('krokorok', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("krokorok", "blink") }
        val look = quirk { bedrockStateful("krokorok", "look_quirk") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("krokorok", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, look),
            condition = { !it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("krokorok", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("krokorok", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, look),
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("krokorok", "battle_idle")
            )
        )
    }
}