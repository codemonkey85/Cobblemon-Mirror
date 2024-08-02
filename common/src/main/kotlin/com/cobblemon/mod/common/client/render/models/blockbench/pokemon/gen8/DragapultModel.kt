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
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class DragapultModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dragapult")
    override val head = getPart("head")

    override var portraitScale = 1.79F
    override var portraitTranslation = Vec3(-0.62, 1.47, 0.0)

    override var profileScale = 0.45F
    override var profileTranslation = Vec3(0.0, 0.73, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var ui_poses: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("dragapult", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dragapult", "blink") }

        ui_poses = registerPose(
            poseName = "ui_poses",
            poseTypes = PoseType.UI_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("dragapult", "ground_idle")
            )
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STANDING_POSES,
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -16)
                ),
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("dragapult", "ground_idle")
                )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -16)
                ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dragapult", "ground_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dragapult", "faint") else null
}