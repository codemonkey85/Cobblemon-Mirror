/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scheduling

import java.util.concurrent.CompletableFuture
import com.cobblemon.mod.common.util.runOnServer

@Deprecated("Use afterOnServer or afterOnClient; ambiguous side is not good for your health")
@JvmOverloads
fun after(ticks: Int = 0, seconds: Float = 0F, serverThread: Boolean = false, action: () -> Unit) {
    val scheduler = if (serverThread) ServerTaskTracker else ClientTaskTracker
    scheduler.after(seconds = seconds + ticks / 20F, action)
}

fun delayedFuture(ticks: Int = 0, seconds: Float = 0F, serverThread: Boolean = false): CompletableFuture<Unit> {
    val future = CompletableFuture<Unit>()
    if (ticks == 0 && seconds == 0F) {
        future.complete(Unit)
    } else {
        after(ticks = ticks, seconds = seconds, serverThread = serverThread) { future.complete(Unit) }
    }
    return future
}

/**
 * Delayed task is created to run on the main thread. This is for when the task
 * being completed after the delay does things like entity removal or other thread-unsafe actions.
 */
@JvmOverloads
fun afterOnServer(ticks: Int = 0, seconds: Float = 0F, action: () -> Unit) = ServerTaskTracker.after(seconds + ticks / 20F, action)
@JvmOverloads
fun afterOnClient(ticks: Int = 0, seconds: Float, action: () -> Unit) = ClientTaskTracker.after(seconds + ticks / 20F, action)

@Deprecated("Use lerpOnServer or lerpOnClient, side-ambiguity causes problems now")
fun lerp(seconds: Float = 0F, serverThread: Boolean = false, action: (Float) -> Unit) = (if (serverThread) ServerTaskTracker else ClientTaskTracker).lerp(seconds, action = action)

@JvmOverloads
fun lerpOnServer(seconds: Float = 0F, action: (Float) -> Unit) = ServerTaskTracker.lerp(seconds = seconds, action = action)
@JvmOverloads
fun lerpOnClient(seconds: Float = 0F, action: (Float) -> Unit) = ClientTaskTracker.lerp(seconds = seconds, action = action)
fun taskBuilder() = ScheduledTask.Builder()

fun delayedFuture(seconds: Float): CompletableFuture<Unit> {
    val future = CompletableFuture<Unit>()
    afterOnServer(seconds = seconds) {
        future.complete(Unit)
    }
    return future
}