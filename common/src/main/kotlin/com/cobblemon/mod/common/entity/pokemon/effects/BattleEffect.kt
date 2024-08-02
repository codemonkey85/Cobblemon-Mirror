/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.effects

import com.cobblemon.mod.common.api.entity.pokemon.EntityEffect
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.concurrent.CompletableFuture

/**
 * An [EntityEffect] that's applied due to a battle-related condition.
 *
 * @author Segfault Guy
 * @since March 5th, 2024
 */
abstract class BattleEffect : EntityEffect {

    open val battleOnly = false

    override fun start(entity: PokemonEntity): CompletableFuture<PokemonEntity>? {
        val progress = entity.effects.progress?.isDone
        if (progress == true || progress == null) {
            val future = CompletableFuture<PokemonEntity>()
            entity.effects.progress = future
            apply(entity, future)
            return future
        }
        else {
            return null
        }
    }

    override fun end(entity: PokemonEntity): CompletableFuture<PokemonEntity>? {
        val progress = entity.effects.progress?.isDone
        if (progress == true || progress == null) {
            val future = CompletableFuture<PokemonEntity>()
            entity.effects.progress = future
            revert(entity, future)
            return future
        }
        else {
            return null
        }
    }

    /**
     * Applies the effect to the provided [PokemonEntity].
     *
     * @param future The future to complete once the effect is applied.
     */
    protected abstract fun apply(entity: PokemonEntity, future: CompletableFuture<PokemonEntity>)

    /**
     * Reverts the effect from the provided [PokemonEntity].
     *
     * @param future The future to complete once the effect is reverted.
     */
    protected abstract fun revert(entity: PokemonEntity, future: CompletableFuture<PokemonEntity>)
}
