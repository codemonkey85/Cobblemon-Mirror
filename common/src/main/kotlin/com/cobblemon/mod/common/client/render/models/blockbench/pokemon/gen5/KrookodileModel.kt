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

class KrookodileModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("krookodile")

    override var portraitScale = 1.26F
    override var portraitTranslation = Vec3(-0.57, 1.6, 0.0)

    override var profileScale = 0.52F
    override var profileTranslation = Vec3(0.06, 0.91, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battle_idle: CobblemonPose

    override fun registerPoses() {
//        animations["physical"] = "q.bedrock_primary('krookodile', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["special"] = "q.bedrock_primary('krookodile', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["status"] = "q.bedrock_primary('krookodile', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
//        animations["recoil"] = "q.bedrock_stateful('krookodile', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('krookodile', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('krookodile', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("krookodile", "blink") }
        val look = quirk { bedrockStateful("krookodile", "look_quirk") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("krookodile", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink, look),
            condition = { !it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("krookodile", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("krookodile", "ground_walk")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, look),
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                bedrock("krookodile", "battle_idle")
            )
        )
    }
}