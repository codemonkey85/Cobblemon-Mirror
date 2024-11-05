/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.platform.events.RenderEvent

/**
 * Maintains a queue of [RenderEventConsumer]s for renders that need to be deferred until the specified [RenderEvent.Stage]
 * occurs in the level renderer.
 *
 * Useful for changing the order of a render.
 *
 * @author Segfault Guy
 * @since September 11th, 2024
 */
object DeferredRenderer {

    private val renderQueues = HashMap<RenderEvent.Stage, ArrayDeque<RenderEventConsumer>>()

    fun enqueue(stage: RenderEvent.Stage, consumer: RenderEventConsumer) = renderQueues.computeIfAbsent(stage) { ArrayDeque() }.add(consumer)

    fun clear(stage: RenderEvent.Stage) = renderQueues[stage]?.clear()

    fun clearAll() = renderQueues.clear()

    init {
        PlatformEvents.RENDER.subscribe { event ->
            val queue = renderQueues[event.stage] ?: return@subscribe
            while (queue.isNotEmpty()) {
                queue.first().invoke(event)
                queue.removeFirst()
            }
        }
    }
}

typealias RenderEventConsumer = (RenderEvent) -> Unit
