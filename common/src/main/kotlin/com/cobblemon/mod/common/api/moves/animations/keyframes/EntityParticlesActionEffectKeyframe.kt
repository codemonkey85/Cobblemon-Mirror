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
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import java.util.concurrent.CompletableFuture
import net.minecraft.server.world.ServerWorld

/**
 * Spawns particles on the entity based on the given particle effect and locator.
 *
 * @author Hiroku
 * @since January 21st, 2024
 */
class EntityParticlesActionEffectKeyframe : ConditionalActionEffectKeyframe(), EntityConditionalActionEffectKeyframe {
    override val entityCondition = "q.entity.is_user".asExpressionLike()
    var effect: String? = null
    var locator: String = "root"
    val delay: ExpressionLike = "0".asExpressionLike()
    val visibilityRange = 200

    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        val entities = context.providers
            .filterIsInstance<EntityProvider>()
            .flatMap { prov -> prov.entities.filter { test(context, it, isUser = prov is UsersProvider) } }

        val effectIdentifier = try {
            effect?.asExpressionLike()?.resolveString(context.runtime)?.takeIf { it != "0.0" } ?: effect
        } catch (e: Exception) {
            effect
        }?.asIdentifierDefaultingNamespace() ?: return skip()

        entities.filter { it is Poseable }.forEach { entity ->
            val packet = SpawnSnowstormEntityParticlePacket(effectIdentifier, entity.id, locator)
            val players = (entity.world as ServerWorld).getPlayers { it.distanceTo(entity) <= visibilityRange }
            packet.sendToPlayers(players)
        }

        return delayedFuture(seconds = delay.resolveFloat(context.runtime), serverThread = true)
    }
}