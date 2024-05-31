/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.render.models.blockbench.animation.SingleBoneLookAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.SimpleQuirk
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.normalizeToArray
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveObject
import com.cobblemon.mod.common.util.singularToPluralList
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.util.math.Vec3d

class JsonPose(model: PosableModel, json: JsonObject) {
    class JsonPoseTransition(val to: String, val animation: ExpressionLike)

    val runtime = MoLangRuntime().setup().setupClient().also {
        it.environment.query.addFunctions(model.functions.functions)
    }

    val condition: ExpressionLike = json.singularToPluralList("condition").get("condition")?.normalizeToArray()?.map { it.asString }?.asExpressionLike() ?: "true".asExpressionLike()

    val poseName = json.get("poseName")?.asString ?: "pose"
    val poseTypes = (json.get("poseTypes")?.asJsonArray?.map { name ->
        PoseType.values().find { it.name.lowercase() == name.asString.lowercase() }
            ?: throw IllegalArgumentException("Unknown pose type ${name.asString}")
    } ?: emptyList()) + if (json.get("allPoseTypes")?.asBoolean == true) PoseType.values().toList() else emptyList()
    val transformTicks = json.get("transformTicks")?.asInt ?: 10
    val transformedParts = json.get("transformedParts")?.asJsonArray?.map {
        it as JsonObject
        val partName = it.get("part").asString
        val part = model.getPart(partName).createTransformation()
        val rotation = it.get("rotation")?.asJsonArray?.let { Vec3d(it[0].asDouble, it[1].asDouble, it[2].asDouble) } ?: Vec3d.ZERO
        val position = it.get("position")?.asJsonArray?.let { Vec3d(it[0].asDouble, it[1].asDouble, it[2].asDouble) } ?: Vec3d.ZERO
        val isVisible = it.get("isVisible")?.asString?.asExpressionLike()
        return@map part.withPosition(position.x, position.y, position.z).withRotationDegrees(rotation.x, rotation.y, rotation.z).also { if (isVisible != null) it.withVisibility(isVisible) }
    }?.toTypedArray() ?: arrayOf()

    val idleAnimations = (json.get("animations")?.asJsonArray ?: JsonArray()).asJsonArray.mapNotNull {

        val condition = if (it is JsonObject) {
            it.get("condition")?.asString?.asExpressionLike() ?: "true".asExpressionLike()
        } else {
            "true".asExpressionLike()
        }
        val animString = if (it is JsonObject) {
            it.get("animation")?.asString ?: return@mapNotNull null
        } else {
            it.asString
        }

        val animation = if (animString == "look") {
            if (model is HeadedFrame) {
                model.singleBoneLook()
            } else {
                SingleBoneLookAnimation(bone = model.relevantPartsByName["head_ai"] ?: model.relevantPartsByName["head"])
            }
        } else {
            try {
                val expression = animString.asExpressionLike()
                runtime.resolveObject(expression).obj as StatelessAnimation
            } catch (exception: Exception) {
                val animString = it.asString
                val anim = animString.substringBefore("(")
                if (JsonPosableModel.ANIMATION_FACTORIES.contains(anim)) {
                    JsonPosableModel.ANIMATION_FACTORIES[anim]!!.stateless(model, animString)
                } else {
                    null
                }
            }
        }

        if (animation == null) {
            null
        } else {
            animation.condition = { it.runtime.resolveBoolean(condition) }
            animation
        }
    }.toTypedArray()

    val quirks = (json.get("quirks")?.asJsonArray ?: JsonArray()).map { json ->
        if (json is JsonPrimitive) {
            return@map json.asString.asExpressionLike().resolveObject(runtime).obj as SimpleQuirk
        }

        json as JsonObject
        json.singularToPluralList("animation")
        val animations: (state: PosableState) -> List<StatefulAnimation> = { _ ->
            (json.get("animations")?.normalizeToArray()?.asJsonArray ?: JsonArray()).mapNotNull { animJson ->
                try {
                    val expr = animJson.asString.asExpressionLike()
                    runtime.resolveObject(expr).obj as StatefulAnimation
                } catch (e: Exception) {
                    val animString = animJson.asString
                    val anim = animString.substringBefore("(")
                    if (JsonPosableModel.ANIMATION_FACTORIES.contains(anim)) {
                        return@mapNotNull JsonPosableModel.ANIMATION_FACTORIES[anim]!!.stateful(model, animString)
                    } else {
                        return@mapNotNull null
                    }
                }
            }
        }

        val loopTimes = json.get("loopTimes")?.asInt ?: 1
        val minSeconds = json.get("minSecondsBetweenOccurrences")?.asFloat ?: 8F
        val maxSeconds = json.get("maxSecondsBetweenOccurrences")?.asFloat ?: 30F
        val condition = json.get("condition")?.asString?.asExpressionLike() ?: "true".asExpressionLike()

        model.quirkMultiple(
            secondsBetweenOccurrences = minSeconds to maxSeconds,
            condition = { it.runtime.resolveBoolean(condition) },
            loopTimes = 1..loopTimes,
            animations = animations
        )
    }

    val animations = json.get("namedAnimations")?.takeIf { it is JsonObject }?.asJsonObject?.let {
        val map = mutableMapOf<String, ExpressionLike>()
        for ((key, value) in it.entrySet()) {
            map[key] = value.asString.asExpressionLike()
        }
        map
    } ?: mutableMapOf()

    val transitions = json.get("transitions")?.takeIf { it is JsonObject }?.asJsonObject?.entrySet()?.map { (key, value) ->
        JsonPoseTransition(key, value.asString.asExpressionLike())
    } ?: emptyList()
}