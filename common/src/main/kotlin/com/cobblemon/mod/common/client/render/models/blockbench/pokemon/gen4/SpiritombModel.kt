/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SpiritombModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("spiritomb")

    override var portraitScale = 1.3F
    override var portraitTranslation = Vec3(-0.4, 0.3, 0.0)

    override var profileScale = 0.75F
    override var profileTranslation = Vec3(-0.2, 0.6, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override fun registerPoses() {

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("spiritomb", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("spiritomb", "ground_idle")
                )
        )
    }
}