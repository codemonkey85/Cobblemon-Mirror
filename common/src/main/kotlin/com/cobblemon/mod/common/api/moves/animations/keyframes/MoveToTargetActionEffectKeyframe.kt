/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.moves.animations.TargetsProvider
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonNavigation
import com.cobblemon.mod.common.util.asExpressionLike
import java.util.concurrent.CompletableFuture
import kotlin.math.pow
import kotlin.math.sqrt
import net.minecraft.resources.ResourceLocation

class MoveToTargetActionEffectKeyframe : ActionEffectKeyframe {
    val speed = 1F
    val timeout = "4".asExpressionLike()
    var proximity = -1F
    val timeoutActionEffect: ResourceLocation? = null

    override fun play(context: ActionEffectContext): CompletableFuture<Unit> {
        val user = context.findOneProvider<UsersProvider>()?.entities?.firstOrNull() as? PokemonEntity ?: return CompletableFuture.completedFuture(Unit)
        val target = context.findOneProvider<TargetsProvider>()?.entities?.firstOrNull() ?: return CompletableFuture.completedFuture(Unit)

        val future = CompletableFuture<Unit>()

        var timedOut = false

        val proximity = this.proximity.takeIf { it != -1F } ?: (sqrt(2 * user.boundingBox.xsize.pow(2)) + 1.5F + sqrt(2 * target.boundingBox.xsize.pow(2))).toFloat()

        if (target.distanceTo(user) !in proximity..20F) {
            future.complete(Unit)
            return future
        }

        val timeoutEffect = timeoutActionEffect?.let { ActionEffects.actionEffects[it] } ?: ActionEffectTimeline.NONE

        val nav = user.navigation
        val navContext = PokemonNavigation.NavigationContext(
            destinationProximity = proximity,
            onArrival = {
                if (!future.isDone && !timedOut) {
                    future.complete(Unit)
                }
            },
            onCannotReach = {
                if (!future.isDone && !timedOut) {
                    timedOut = true
                    timeoutEffect.run(context).thenApply { future.complete(Unit) }
                }
            }
        )

        afterOnServer(seconds = timeout.resolveFloat(context.runtime)) {
            if (!future.isDone && !timedOut) {
                timedOut = true
                timeoutEffect.run(context).thenApply { future.complete(Unit) }
                nav.stop()
            }
        }

        nav.moveTo(target.x, target.y, target.z, speed.toDouble(), navContext)
        return future
    }
}