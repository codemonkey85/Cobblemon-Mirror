/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import java.util.concurrent.CompletableFuture
import net.minecraft.resources.ResourceLocation

/**
 * An action effect keyframe that simply runs another action effect based on an identifier. This action
 * effect is marked complete only when the action effect it runs is complete unless [waitForActionEffect] is
 * false.
 *
 * @author Hiroku
 * @since January 21st, 2024
 */
class RunActionEffectKeyframe : ConditionalActionEffectKeyframe() {
    val actionEffect: ResourceLocation? = null
    val waitForActionEffect = true
    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        return if (actionEffect != null) {
            ActionEffects.actionEffects[actionEffect]?.run(context)?.takeIf { waitForActionEffect } ?: skip()
        } else {
            skip()
        }
    }
}