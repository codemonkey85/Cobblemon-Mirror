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
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DreepyModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dreepy")
    override val head = getPart("head")

    override var portraitScale = 2.66F
    override var portraitTranslation = Vec3(-0.27, -1.94, 0.0)

    override var profileScale = 0.81F
    override var profileTranslation = Vec3(0.0, 0.35, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    var shoulderOffset = 4.5

    override val cryAnimation = CryProvider { bedrockStateful("dreepy", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dreepy", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dreepy", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dreepy", "ground_idle")
            )
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                    singleBoneLook(),
                    bedrock("dreepy", "ground_idle")
                ),
                transformedParts = arrayOf(
                    rootPart.createTransformation().addPosition(shoulderOffset, -2, 0),
                    rootPart.createTransformation().addRotationDegrees(ModelPartTransformation.Z_AXIS, 12.5)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                    singleBoneLook(),
                    bedrock("dreepy", "ground_idle")
                ),
                transformedParts = arrayOf(
                    rootPart.createTransformation().addPosition(-shoulderOffset, -2, 0),
                    rootPart.createTransformation().addRotationDegrees(ModelPartTransformation.Z_AXIS, -12.5)
                )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dreepy", "faint") else null
}