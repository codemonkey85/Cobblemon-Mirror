/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scheduling

import java.util.concurrent.CompletableFuture
import kotlin.math.max

/**
 * A task to be executed synchronously. The precision of tasks is such that the delay
 * is never more than one tick from the scheduled time on the server, and bound to render
 * speeds on the client. Pinched from Cable Libs and expanded.
 *
 * @author landonjw, Hiroku
 */
class ScheduledTask(
    /** The action to run. */
    val action: (ScheduledTask) -> Unit,
    /** The identifier, optional. */
    val identifier: String? = null,
    /** How long until the task should execute. */
    delaySeconds: Float,
    /** The seconds between each execution, if this is repeated. */
    val intervalSeconds: Float = -1F,
    /** The number of times this task should iterate. */
    val iterations: Int = 1
) {
    companion object {
        val BLANK = ScheduledTask({}, delaySeconds = 0F)
    }
    val future = CompletableFuture<Unit>()
    var secondsPassed = 0F
    var currentIteration: Int = 0
        private set
    var secondsRemaining: Float = 0F
        private set
    var expired = false
        private set
    var paused = false
        set(value) {
            field = value
        }

    init {
        if (delaySeconds > 0) {
            secondsRemaining = delaySeconds
        }
    }

    override fun toString(): String {
        return identifier?.let { "Task-$it" } ?: super.toString()
    }

    fun update(deltaSeconds: Float) {
        if (!expired && !paused) {
            secondsPassed += deltaSeconds
            secondsRemaining = max(0F, secondsRemaining - deltaSeconds)
            if (secondsRemaining == 0F) {
                action(this)
                currentIteration++
                if (intervalSeconds != -1F && (currentIteration < iterations || iterations == -1)) {
                    secondsRemaining = intervalSeconds
                } else {
                    expired = true
                }
            }
        }
    }

    fun expire() {
        this.expired = true
        future.complete(Unit)
    }

    class Builder {
        private var action: ((ScheduledTask) -> Unit)? = null

        /** The number of seconds before the task will first be executed. */
        private var delaySeconds: Float = 0F

        /** The number of seconds before the task will be executed after it's already executed. */
        private var interval: Float = -1F

        /** The number of times the task will run. -1 to run indefinitely. */
        private var iterations: Int = 1
        
        private var identifier: String? = null

        private var tracker: SchedulingTracker? = null

        /**
         * Sets the consumer to be executed by the task.
         *
         * @param action the consumer to be executed by the task
         * @return builder with consumer set
         */
        fun execute(action: ((ScheduledTask) -> Unit)): Builder {
            this.action = action
            return this
        }

        /**
         * Sets the delay of the task.
         *
         * @param delaySeconds the number of seconds before the task will first be executed
         * @return builder with delay set
         * @throws IllegalArgumentException if delay is below 0
         */
        fun delay(delaySeconds: Float): Builder {
            require(delaySeconds >= 0) { "Delay must not be below 0" }
            this.delaySeconds = delaySeconds
            return this
        }

        /**
         * Sets the interval between task executions.
         *
         * @param interval the number of seconds before the task will execute after already executing
         * @return builder with interval set
         * @throws IllegalArgumentException if interval is below 0
         */
        fun interval(interval: Float): Builder {
            require(interval >= 0) { "Interval must not be below 0" }
            this.interval = interval
            return this
        }

        /**
         * Sets the number of times the task will run.
         *
         * @param iterations the number of times the task will run, -1 treated as indefinitely
         * @return builder with number of iterations set
         * @throws IllegalArgumentException if iterations is below -1
         */
        fun iterations(iterations: Int): Builder {
            require(iterations >= -1) { "Iterations must not be below -1" }
            this.iterations = iterations
            return this
        }

        /**
         * Sets the task to run indefinitely.
         *
         * @return builder with infinite iterations set
         */
        fun infiniteIterations(): Builder {
            return iterations(-1)
        }

        fun tracker(schedulingTracker: SchedulingTracker): Builder {
            this.tracker = schedulingTracker
            return this
        }

        /**
         * Builds the task using the properties of the task builder.
         * This will add the task to [TaskTickListener].
         *
         * @return a task with the task builder's properties
         * @throws IllegalStateException if consumer is not set
         */
        fun build(): ScheduledTask {
            checkNotNull(action) { "action must be set" }
            val task = ScheduledTask(
                action = action!!,
                identifier = identifier,
                delaySeconds = delaySeconds,
                intervalSeconds = interval,
                iterations = iterations
            )
            (tracker ?: ServerTaskTracker).addTask(task)
            return task
        }
    }
}