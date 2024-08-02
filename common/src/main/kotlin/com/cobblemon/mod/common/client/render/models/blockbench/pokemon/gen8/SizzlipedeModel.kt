/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SizzlipedeModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("sizzlipede")
    override val head = getPart("head")

    override var portraitScale = 3.24F
    override var portraitTranslation = Vec3(-0.89, -3.35, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3(0.07, 0.21, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("sizzlipede", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("sizzlipede", "blink") }

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformTicks = 10,
                animations = arrayOf(bedrock("sizzlipede", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
                condition = { !it.isBattling },
                quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sizzlipede", "ground_idle")
            )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling },
                animations = arrayOf(
                        bedrock("sizzlipede", "battle_idle")
                )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
                quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("sizzlipede", "ground_walk")
            )
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("sizzlipede", "shoulder_left")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, 1.3),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, 3.8),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Z_AXIS, 1.1)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("sizzlipede", "shoulder_right")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, 1.3),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -3.8),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Z_AXIS, 1.1)
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("sizzlipede", "faint") else null
}