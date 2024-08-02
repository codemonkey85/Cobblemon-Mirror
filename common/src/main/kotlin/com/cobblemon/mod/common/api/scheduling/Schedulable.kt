/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scheduling

import java.util.concurrent.CompletableFuture

/**
 * An interface for which an implementation must provide a [SchedulingTracker] so that the standard scheduling
 * functions can be executed.
 *
 * @author Hiroku
 * @since November 5th, 2023
 */
interface Schedulable {
    val schedulingTracker: SchedulingTracker

    fun momentarily(action: () -> Unit): ScheduledTask = after(action = action)
    fun after(seconds: Float = 0F, action: () -> Unit): ScheduledTask {
        return schedulingTracker.addTask(
            ScheduledTask(
                action = { action() },
                delaySeconds = seconds
            )
        )
    }

    fun lerp(seconds: Float = 0F, action: (Float) -> Unit): ScheduledTask {
        var passed = 0F
        if (seconds == 0F) {
            action(1F)
            return ScheduledTask.BLANK
        }
        action(passed / seconds)
        return if (passed / seconds != 1F) {
            taskBuilder().tracker(schedulingTracker).interval(0F).iterations(-1).execute { task ->
                passed = task.secondsPassed
                if (passed > seconds) {
                    passed = seconds
                }
                val ratio = passed / seconds
                action(ratio)
                if (passed >= seconds) {
                    task.expire()
                }
            }.build()
        } else {
            ScheduledTask.BLANK
        }
    }

    fun delayedFuture(seconds: Float): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        after(seconds = seconds) {
            future.complete(Unit)
        }
        return future
    }

    fun taskBuilder(): ScheduledTask.Builder = ScheduledTask.Builder().tracker(schedulingTracker)
}