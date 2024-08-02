/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository

/**
 * An [AnimationReferenceFactory] that loads Bedrock-format animations from [BedrockAnimationRepository].
 *
 * The expected format is "bedrock(animation_group, animation_name)"
 *
 * @author Hiroku
 * @since June 28th, 2023
 */
object BedrockAnimationReferenceFactory : AnimationReferenceFactory {
    override fun pose(model: PosableModel, animString: String): PoseAnimation {
        val split = animString.replace("bedrock(", "").replace(")", "").split(",").map(String::trim)
        return model.bedrock(animationGroup = split[0], animation = split[1])
    }

    override fun active(model: PosableModel, animString: String, ): ActiveAnimation {
        val split = animString.replace("bedrock(", "").replace(")", "").split(",").map(String::trim)
        return model.bedrockStateful(
            animationGroup = split[0],
            animation = split[1]
        )
    }
}