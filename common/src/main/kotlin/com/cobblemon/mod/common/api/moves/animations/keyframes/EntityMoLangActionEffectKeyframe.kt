/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.EntityProvider
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.scheduling.delayedFuture
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket
import com.cobblemon.mod.common.util.asExpressionLike
import java.util.concurrent.CompletableFuture
import net.minecraft.server.level.ServerLevel

/**
 * An action effect keyframe that will run client-side MoLang for all entities that fit the entity condition.
 *
 * This MoLang executes within that entity's runtime, meaning you can trigger animations or locational particles
 * or sounds.
 *
 * @author Hiroku
 * @since October 30th, 2023
 */
class EntityMoLangActionEffectKeyframe : ConditionalActionEffectKeyframe(), EntityConditionalActionEffectKeyframe {
    var delay = "0".asExpressionLike()
    val expressions = mutableSetOf<String>()
    val visibilityRange = 200
    override val entityCondition: ExpressionLike = "q.entity.is_user".asExpressionLike()

    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        val entities = context.providers
            .filterIsInstance<EntityProvider>()
            .flatMap { prov -> prov.entities.filter { test(context, it, isUser = prov is UsersProvider) } }

        for (entity in entities) {
            val world = entity.level() as ServerLevel
            val players = world.getPlayers { it.distanceTo(entity) <= visibilityRange }
            val pkt = RunPosableMoLangPacket(entityId = entity.id, expressions = expressions)
            players.forEach { it.sendPacket(pkt) }
        }

        return delayedFuture(seconds = delay.resolveFloat(context.runtime))
    }
}
