/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pose

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType

typealias CobblemonPose = Pose

/**
 * A pose for a model. This is a collection of [animations] and [transformedParts] that should be applied to a model.
 * It also contains any number of [namedAnimations], [ModelQuirk]s, a [condition] for the pose to ever be triggered,
 * and the different [poseTypes] for which this pose is appropriate for.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
class Pose(
    var poseName: String,
    val poseTypes: Set<PoseType>,
    val condition: ((PosableState) -> Boolean)?,
    /** What to do after the pose is transitioned into completely. */
    val onTransitionedInto: (PosableState) -> Unit = {},
    /** If there are no dedicated transition animations, the interpolation animation will take this many ticks. */
    val transformTicks: Int,
    val namedAnimations: MutableMap<String, ExpressionLike> = mutableMapOf(),
    val animations: Array<PoseAnimation>,
    val transformedParts: Array<ModelPartTransformation>,
    val quirks: Array<ModelQuirk<*>>
) {
    fun isSuitable(state: PosableState) = condition?.invoke(state) ?: true

    val transitions = mutableMapOf<String, (Pose, Pose) -> ActiveAnimation>()

    fun apply(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float) {
        animations.filter { state.shouldIdleRun(it, 0F) }.forEach { animation ->
            animation.apply(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, state.getIdleIntensity(animation))
        }
    }
}