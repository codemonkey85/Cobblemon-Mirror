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
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3


class FalinksModel (root: ModelPart) : PokemonPosableModel(root){
    override val rootPart = root.registerChildWithAllChildren("falinks")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3(-1.0, -1.2, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.1, 0.9, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var battlestanding: CobblemonPose
    lateinit var battlestanding2: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var uipose: CobblemonPose
    lateinit var sleep: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("falinks", "blink") }
        val blink2 = quirk { bedrockStateful("falinks", "blink2") }
        val blink3 = quirk { bedrockStateful("falinks", "blink3") }
        val blink4 = quirk { bedrockStateful("falinks", "blink4") }
        val blink5 = quirk { bedrockStateful("falinks", "blink5") }
        val blink6 = quirk { bedrockStateful("falinks", "blink6") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("falinks", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("falinks", "ground_idle")
            )
        )

        battlestanding2 = registerPose(
            poseName = "battlestanding2",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { it.isBattling && ((it.getEntity()?.uuid?.mostSignificantBits ?: 2) % 2).toInt() == 0 },
            animations = arrayOf(
                bedrock("falinks", "battle_idle2")
            )
        )

        battlestanding = registerPose(
            poseName = "battlestanding",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("falinks", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { !it.isBattling },
            animations = arrayOf(
                bedrock("falinks", "ground_walk")
            )
        )

        uipose = registerPose(
            poseName = "uipose",
            poseTypes = PoseType.UI_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            animations = arrayOf(
                bedrock("falinks", "summary_idle")
            )
        )
    }
}