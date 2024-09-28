/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.fossil

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.entity.PoseType
import com.google.gson.annotations.SerializedName
import net.minecraft.client.model.geom.ModelPart

/**
 * A model for rendering in the restoration tank. This model is not intended to be used for living entities.
 *
 * @author Hiroku
 * @since October 30th, 2023
 */
class FossilModel(root: Bone) : PosableModel(root) {
    @Transient
    @SerializedName("Something that isn't root part. Gson thinks they're the same as the root field and so field duplication. Stupid.")
    override val rootPart = (root as ModelPart).registerChildWithAllChildren(root.children.entries.first().key)
    // Represents a very rough middle of the model
    // The reason to do this is to
    // 1. The embryo is aligned with the center of the model
    // 2. The model can appear to grow (scale) from the center out
    var yGrowthPoint = 0F
    var maxScale = 1F
    var yTranslation = 0F // Offset inside the tank
    var tankAnimations: Array<PoseAnimation> = emptyArray()
    var tankQuirks: Array<ModelQuirk<*>> = emptyArray()

    override fun registerPoses() {
        registerPose(
            poseType = PoseType.SLEEP,
            animations = tankAnimations,
            quirks = tankQuirks
        )
    }
}