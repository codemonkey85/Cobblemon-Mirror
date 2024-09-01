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
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class KingdraModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("kingdra")
    override val head = getPart("head")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.4, 1.8, 0.0)

    override var profileScale = 0.63F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var float: Pose
    lateinit var swim: Pose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("kingdra", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(Y_AXIS, -4.0)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "ground_walk")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(Y_AXIS, -4.0)
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "water_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(Y_AXIS, -4.0)
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "water_swim")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(Y_AXIS, -4.0)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("kingdra", "faint") else null
}