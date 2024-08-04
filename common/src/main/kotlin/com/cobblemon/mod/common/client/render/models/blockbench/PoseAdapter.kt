/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isDusk
import com.cobblemon.mod.common.util.isStandingOnRedSand
import com.cobblemon.mod.common.util.isStandingOnSand
import com.cobblemon.mod.common.util.isStandingOnSandOrRedSand
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * An adapter for deserializing a [Pose] object from a JSON object.
 *
 * @author Hiroku
 * @since October 18th, 2022
 */
class PoseAdapter(
    val poseConditionReader: (JsonObject) -> List<(PosableState) -> Boolean>,
    val modelFinder: () -> PosableModel
) : JsonDeserializer<Pose> {
    companion object {
        val runtime = MoLangRuntime()
    }

    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Pose {
        val model = modelFinder()
        val obj = json as JsonObject
        val pose = JsonPose(model, obj)

        val conditionsList = mutableListOf<(PosableState) -> Boolean>()
        val mustBeTouchingWater = json.get("isTouchingWater")?.asBoolean
        if (mustBeTouchingWater != null) {
            conditionsList.add { mustBeTouchingWater == it.getEntity()?.isInWater }
        }

        val mustBeTouchingWaterOrRain = json.get("isInWaterOrRain")?.asBoolean
        if (mustBeTouchingWaterOrRain != null) {
            conditionsList.add { mustBeTouchingWaterOrRain == it.getEntity()?.isInWaterOrRain }
        }
        val mustBeSubmergedInWater = json.get("isUnderWater")?.asBoolean
        if (mustBeSubmergedInWater != null) {
            conditionsList.add { mustBeSubmergedInWater == it.getEntity()?.isUnderWater }
        }
        val mustBeStandingOnRedSand = json.get("isStandingOnRedSand")?.asBoolean
        if (mustBeStandingOnRedSand != null) {
            conditionsList.add { mustBeStandingOnRedSand == it.getEntity()?.isStandingOnRedSand() }
        }
        val mustBeStandingOnSand = json.get("isStandingOnSand")?.asBoolean
        if (mustBeStandingOnSand != null) {
            conditionsList.add { mustBeStandingOnSand == it.getEntity()?.isStandingOnSand() }
        }
        val mustBeStandingOnSandOrRedSand = json.get("isStandingOnSandOrRedSand")?.asBoolean
        if (mustBeStandingOnSandOrRedSand != null) {
            conditionsList.add { mustBeStandingOnSandOrRedSand == it.getEntity()?.isStandingOnSandOrRedSand() }
        }
        val mustBeDusk = json.get("isDusk")?.asBoolean
        if (mustBeDusk != null) {
            conditionsList.add { mustBeDusk == it.getEntity()?.isDusk() }
        }

        conditionsList.addAll(poseConditionReader(json))

        if (json.has("conditions")) {
            val conditionSet = json.get("conditions").asJsonArray.map { it.asString.asExpressionLike() }
            conditionsList.add { state ->
                val entity = state.getEntity()
                if (entity is PosableEntity) {
                    conditionSet.any { it.resolveBoolean(state.runtime) }
                } else {
                    false
                }
            }
        }

        val poseCondition: (PosableState) -> Boolean = if (conditionsList.isEmpty()) { { true } } else conditionsList.reduce { acc, function -> { acc(it) && function(it) } }

        return Pose(
            poseName = pose.poseName,
            poseTypes = pose.poseTypes.toSet(),
            condition = poseCondition,
            transformTicks = pose.transformTicks,
            animations = pose.idleAnimations,
            transformedParts = pose.transformedParts,
            quirks = pose.quirks.toTypedArray()
        ).also {
            it.transitions.putAll(
                pose.transitions
                    .mapNotNull<JsonPose.JsonPoseTransition, Pair<String, (Pose, Pose) -> ActiveAnimation>> {
                        it.to to { _, _ -> it.animation.resolveObject(model.runtime).obj as ActiveAnimation }
                    }.toMap()
            )
        }
    }
}