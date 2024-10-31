/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.scheduling.delayedFuture
import com.cobblemon.mod.common.util.asExpressionLike
import java.util.concurrent.CompletableFuture

/**
 * An action effect keyframe that simply executes some MoLang on the effect context's runtime.
 *
 * @author Hiroku
 * @since January 21st, 2024
 */
class MoLangActionEffectKeyframe : ActionEffectKeyframe {
    val expressions: ExpressionLike = "0".asExpressionLike()
    val delay: ExpressionLike = "0".asExpressionLike()
    override fun play(context: ActionEffectContext): CompletableFuture<Unit> {
        expressions.resolve(context.runtime)
        return delayedFuture(seconds = delay.resolveFloat(context.runtime))
    }
}