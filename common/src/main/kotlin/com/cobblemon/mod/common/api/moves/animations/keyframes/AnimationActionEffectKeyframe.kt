/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.EntityProvider
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.scheduling.delayedFuture
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.util.asExpressionLike
import java.util.concurrent.CompletableFuture
import net.minecraft.server.level.ServerLevel

class AnimationActionEffectKeyframe : ConditionalActionEffectKeyframe(), EntityConditionalActionEffectKeyframe {
    var delay = "0".asExpressionLike()
    var visibilityRange = 200
    var animation: Set<String> = setOf("physical")
    var variables: List<Expression> = listOf()
    override val entityCondition = "q.entity.is_user".asExpressionLike()

    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        val entities = context.providers
            .filterIsInstance<EntityProvider>()
            .flatMap { prov -> prov.entities.filter { test(context, it, isUser = prov is UsersProvider) } }

        val expressions = variables.map { it.originalString }.toSet()

        // Treat them as expressions if possible but otherwise yeah just send them as strings
        val animation = animation.map {
            try {
                it.asExpressionLike().resolveString(context.runtime).takeIf { it != "0.0" } ?: it
            } catch (e: Exception) {
                it
            }
        }.toSet()

        for (entity in entities) {
            val world = entity.level() as ServerLevel
            val players = world.getPlayers { it.distanceTo(entity) <= visibilityRange }
            val pkt = PlayPosableAnimationPacket(entity.id, animation = animation, expressions = expressions)
            players.forEach { it.sendPacket(pkt) }
        }

        return delayedFuture(seconds = delay.resolveFloat(context.runtime))
    }
}