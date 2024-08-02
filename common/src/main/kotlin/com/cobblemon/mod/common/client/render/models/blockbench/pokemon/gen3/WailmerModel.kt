/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Z_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class WailmerModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("wailmer")

    val finLeft = getPart("fin_left")
    val finRight = getPart("fin_right")
    val jaw = getPart("jaw")

    override var portraitScale = 1.2F
    override var portraitTranslation = Vec3(-0.15, -0.3, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.3, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 0,
            animations = arrayOf(
                finLeft.rotation(sineFunction(amplitude = 1F / 4, period = 4F), axis = Z_AXIS, timeVariable = { state, _, _ -> state.animationSeconds }),
                finRight.rotation(sineFunction(amplitude = -1F / 4, period = 4F), axis = Z_AXIS, timeVariable = { state, _, _ -> state.animationSeconds }),
                jaw.rotation(sineFunction(amplitude = 0.05F, period = 8F, verticalShift = 0.04F), axis = X_AXIS, timeVariable = { state, _, _ -> state.animationSeconds })
//                bedrock("wailmer", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 0,
            animations = arrayOf(
                finLeft.rotation(sineFunction(amplitude = 1F / 3, period = 3F), axis = Z_AXIS, timeVariable = { state, _, _ -> state.animationSeconds }),
                finRight.rotation(sineFunction(amplitude = -1F / 3, period = 3F), axis = Z_AXIS, timeVariable = { state, _, _ -> state.animationSeconds }),
                jaw.rotation(sineFunction(amplitude = 0.05F, period = 8F, verticalShift = 0.04F), axis = X_AXIS, timeVariable = { state, _, _ -> state.animationSeconds }),
//                bedrock("wailmer", "ground_walk")
            )
        )
    }
}