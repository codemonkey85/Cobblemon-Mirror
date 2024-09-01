/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.EntityProvider
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.scheduling.delayedFuture
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.core.registries.Registries
import java.util.concurrent.CompletableFuture

/**
 * An action effect keyframe that plays a positioned sound for all matching entities.
 *
 * @author Hiroku
 * @since January 21st, 2024
 */
class EntitySoundActionEffectKeyframe : ConditionalActionEffectKeyframe(), EntityConditionalActionEffectKeyframe {
    override val entityCondition = "q.entity.is_user".asExpressionLike()
    var sound: String? = null
    val delay: ExpressionLike = "0".asExpressionLike()

    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        val entities = context.providers
            .filterIsInstance<EntityProvider>()
            .flatMap { prov -> prov.entities.filter { test(context, it, isUser = prov is UsersProvider) } }

        val soundIdentifier = try {
            sound?.asExpressionLike()?.resolveString(context.runtime)?.takeIf { it != "0.0" } ?: sound
        } catch (e: Exception) {
            sound
        }?.asIdentifierDefaultingNamespace() ?: return skip()

        entities.forEach { entity ->
            val soundEvent = entity.level().registryAccess().registryOrThrow(Registries.SOUND_EVENT).get(soundIdentifier) ?: return skip()
            entity.playSound(soundEvent, 1f, 1f)
        }

        return delayedFuture(seconds = delay.resolveFloat(context.runtime))
    }
}