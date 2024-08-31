/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MareepModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("mareep")
    override val head = getPart("head")

    val wool = getPart("wool")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3(-0.5, -1.2, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.0, 0.4, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("mareep", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("mareep", "blink") }
        val isNotSheared = "q.has_aspect('${DataKeys.HAS_BEEN_SHEARED}') == false".asExpressionLike()
        val isSheared = "q.has_aspect('${DataKeys.HAS_BEEN_SHEARED}')".asExpressionLike()
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformedParts = arrayOf(wool.createTransformation().withVisibility(visibility = isNotSheared)),
            animations = arrayOf(bedrock("mareep", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            transformedParts = arrayOf(wool.createTransformation().withVisibility(visibility = isNotSheared)),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mareep", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(wool.createTransformation().withVisibility(visibility = isNotSheared)),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mareep", "ground_walk").withCondition(isNotSheared),
                bedrock("mareep", "ground_walk_sheared").withCondition(isSheared)
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            transformedParts = arrayOf(wool.createTransformation().withVisibility(visibility = isNotSheared)),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mareep", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("mareep", "faint") else null
}