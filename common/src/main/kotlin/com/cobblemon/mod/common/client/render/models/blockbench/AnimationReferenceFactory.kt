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

/**
 * Responsible for creating an animation instance from animation reference strings. In the Bedrock format these often
 * look like "bedrock(pokemon_name, animation_name)" which searches an animation registry for data.
 *
 * This is essentially legacy code as MoLang integration in JSON posers is a much more effective framework for custom
 * animation generation/referencing.
 */
@Deprecated("Use MoLang expressions in JSON posers instead")
interface AnimationReferenceFactory {
    fun pose(model: PosableModel, animString: String): PoseAnimation
    fun active(model: PosableModel, animString: String): ActiveAnimation
}