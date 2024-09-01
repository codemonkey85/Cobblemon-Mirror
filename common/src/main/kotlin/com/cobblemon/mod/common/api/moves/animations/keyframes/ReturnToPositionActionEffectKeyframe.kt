/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.scheduling.after
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonNavigation
import com.cobblemon.mod.common.util.asExpressionLike
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture

class ReturnToPositionActionEffectKeyframe : ActionEffectKeyframe {
    val speed = 1F
    val timeout = "4".asExpressionLike()
    val timeoutActionEffect: ResourceLocation? = null

    override fun play(context: ActionEffectContext): CompletableFuture<Unit> {
        val user = context.findOneProvider<UsersProvider>()?.entities?.firstOrNull() as? PokemonEntity ?: return skip()
        val future = CompletableFuture<Unit>()

        val pos = (context.runtime.environment.getValue(setOf("${user.stringUUID}-pos").iterator())?.value() as? ObjectValue<*>)?.obj as? Vec3 ?: return skip()

        var timedOut = false

        if (pos.distanceTo(user.position()) > 20) {
            future.complete(Unit)
            return future
        }

        val timeoutEffect = timeoutActionEffect?.let { ActionEffects.actionEffects[it] } ?: ActionEffectTimeline.NONE

        val nav = user.navigation
        val navContext = PokemonNavigation.NavigationContext(
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

        after(seconds = timeout.resolveFloat(context.runtime)) {
            if (!future.isDone && !timedOut) {
                timedOut = true
                timeoutEffect.run(context).thenApply { future.complete(Unit) }
                nav.stop()
            }
        }

        nav.moveTo(pos.x, pos.y, pos.z, speed.toDouble(), navContext)
        return future
    }
}