/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import java.util.concurrent.CompletableFuture


/**
 * A scheduled spawning action.
 *
 * @author Hiroku
 * @since February 4th, 2022
 */
abstract class SpawnAction<R>(
    val ctx: SpawningContext,
    open val detail: SpawnDetail
) {
    val future = CompletableFuture<R>().also { it.thenApply { result -> ctx.spawner.afterSpawn(this, result) } }

    /**
     * Does whatever action is required to spawn this detail into the context.
     *
     * @return The result of spawning
     */
    protected abstract fun run(): R?

    /**
     * Runs the spawn action and processes any non-null result.
     */
    open fun complete(): R? {
        if (future.isDone) {
            return null
        }

        ctx.applyInfluences { it.affectAction(this) }
        val result = run()
        if (result != null) {
            future.complete(result)
        } else {
            future.completeExceptionally(Exception("Nothing was spawned."))
        }

        return result
    }
}
