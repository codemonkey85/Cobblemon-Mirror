/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.scheduling.delayedFuture
import com.cobblemon.mod.common.util.asExpressionLike

class PauseActionEffectKeyframe : ConditionalActionEffectKeyframe() {
    val pause = "1".asExpressionLike()
    override fun playWhenTrue(context: ActionEffectContext) = delayedFuture(seconds = pause.resolveFloat(context.runtime))
}