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

class SandileModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("sandile")

    override var portraitScale = 2.54F
    override var portraitTranslation = Vec3(-0.5, -1.9, 0.0)

    override var profileScale = 0.84F
    override var profileTranslation = Vec3(0.05, 0.48, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battle_idle: CobblemonPose

    override fun registerPoses() {
//        animations["physical"] = "q.bedrock_primary('sandile', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["special"] = "q.bedrock_primary('sandile', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["status"] = "q.bedrock_primary('sandile', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["recoil"] = "q.bedrock_stateful('sandile', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('sandile', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('sandile', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("sandile", "blink") }
        val bite = quirk { bedrockStateful("sandile", "bite_quirk") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(bite),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("sandile", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, bite),
            condition = { !it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("sandile", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("sandile", "ground_walk")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, bite),
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("sandile", "battle_idle")
            )
        )
    }
}