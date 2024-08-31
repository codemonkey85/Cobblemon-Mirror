/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.bedrockk.molang.MoLang
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type

/**
 * Gson adapter for converting bedrock/blockbench json data into a friendlier object model.
 *
 * @author landonjw
 * @since January 5, 2022
 */
object BedrockAnimationAdapter : JsonDeserializer<BedrockAnimation> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BedrockAnimation {
        if (json is JsonObject) {
            val animationLength = json["animation_length"]?.asDouble ?: -1.0
            val shouldLoop = animationLength > 0 && json["loop"]?.asBoolean == true
            val boneTimelines = mutableMapOf<String, BedrockBoneTimeline>()
            val effects = mutableListOf<BedrockEffectKeyframe>()
            json["bones"]?.asJsonObject?.entrySet()?.forEach { (boneName, timeline) ->
                boneTimelines[boneName] = deserializeBoneTimeline(timeline.asJsonObject)
            }
            json["particle_effects"]?.asJsonObject?.entrySet()?.forEach { (frame, effectJson) ->
                fun resolveEffect(jsonObject: JsonObject): BedrockParticleKeyframe {
                    val effectId = jsonObject.get("effect").asString.asIdentifierDefaultingNamespace()
                    val effect = BedrockParticleOptionsRepository.getEffect(effectId)
                        ?: throw IllegalArgumentException("Unrecognized particle effect $effectId referenced in animation. Maybe your particle effect isn't named correctly inside the effect file?")
                    val locator = jsonObject.get("locator")?.asString ?: "root"
                    val seconds = frame.toFloat()
                    val scripts = jsonObject.get("pre_effect_script")?.asString?.split("\n")?.map { MoLang.createParser(it).parseExpression() } ?: emptyList()
                    return BedrockParticleKeyframe(
                        seconds = seconds,
                        effect = effect,
                        locator = locator,
                        scripts = scripts
                    )
                }

                if (effectJson is JsonObject) {
                    effects.add(resolveEffect(effectJson))
                } else if (effectJson is JsonArray) {
                    for (effectJsonElement in effectJson) {
                        effects.add(resolveEffect(effectJsonElement as JsonObject))
                    }
                }
            }
            json["sound_effects"]?.asJsonObject?.entrySet()?.forEach { (frame, effectJson) ->
                fun resolveEffect(jsonObject: JsonObject): BedrockSoundKeyframe {
                    val effectId = jsonObject.get("effect").asString.asIdentifierDefaultingNamespace()
                    val seconds = frame.toFloat()
                    return BedrockSoundKeyframe(
                        seconds = seconds,
                        sound = effectId,
                    )
                }

                if (effectJson is JsonObject) {
                    effects.add(resolveEffect(effectJson))
                } else if (effectJson is JsonArray) {
                    for (effectJsonElement in effectJson) {
                        effects.add(resolveEffect(effectJsonElement as JsonObject))
                    }
                }
            }

            json["timeline"]?.asJsonObject?.entrySet()?.forEach { (frame, effectJson) ->
                effects.add(
                    BedrockInstructionKeyframe(
                        seconds = frame.toFloat(),
                        expressions = if (effectJson is JsonArray) effectJson.asExpressionLike() else effectJson.asString.asExpressionLike()
                    )
                )
            }

            return BedrockAnimation(shouldLoop, animationLength, effects, boneTimelines)
        }
        else {
            throw IllegalStateException("animation json could not be parsed")
        }
    }

    private fun deserializeBoneTimeline(bone: JsonObject): BedrockBoneTimeline {
        val positions = if (bone.has("position")) {
            if (bone["position"].isJsonObject) {
                deserializeKeyframe(bone["position"].asJsonObject, Transformation.POSITION)
            } else {
                deserializeMolangBoneValue(bone["position"].asJsonArray, Transformation.POSITION)
            }
        } else {
            EmptyBoneValue
        }
        val rotations = if (bone.has("rotation")) {
            if (bone["rotation"].isJsonObject) {
                deserializeKeyframe(bone["rotation"].asJsonObject, Transformation.ROTATION)
            } else {
                deserializeMolangBoneValue(bone["rotation"].asJsonArray, Transformation.ROTATION)
            }
        } else {
            EmptyBoneValue
        }
        val scale = if (bone.has("scale")) {
            val json = bone["scale"]
            if (json.isJsonObject) {
                deserializeKeyframe(json.asJsonObject, Transformation.SCALE)
            } else if (json.isJsonArray) {
                deserializeMolangBoneValue(json.asJsonArray, Transformation.SCALE)
            } else {
                val str = json.asString
                deserializeMolangBoneValue(JsonArray().also { arr -> repeat(times = 3) { arr.add(JsonPrimitive(str)) } }, Transformation.SCALE)
            }
        } else {
            EmptyBoneValue
        }
        return BedrockBoneTimeline(positions, rotations, scale)
    }

    fun cleanExpression(value: String) =
        (if (value.startsWith("+")) value.substring(1) else value).let {
            if (it.startsWith("-(")) it.replaceFirst("-(", "-1*(") else it
        }.replace("*+", "*").replace("q.", "query.")
            .replace("camera_rotation(0)", "camera_rotation_x").replace("camera_rotation(1)", "camera_rotation_y")

    fun deserializeMolangBoneValue(array: JsonArray, transformation: Transformation): MolangBoneValue {
        try {
            return MolangBoneValue(
                x = MoLang.createParser(cleanExpression(array[0].asString)).parseExpression(),
                y = MoLang.createParser(cleanExpression(array[1].asString)).parseExpression(),
                z = MoLang.createParser(cleanExpression(array[2].asString)).parseExpression(),
                transformation = transformation
            )
        } catch (e: Exception) {
            LOGGER.error(array.joinToString { it.toString() })
            throw e
        }
    }

    private fun deserializeKeyframe(frames: JsonObject, transformation: Transformation): BedrockKeyFrameBoneValue {
        val keyframes = BedrockKeyFrameBoneValue()
        frames.entrySet().forEach { (time, keyframeJson) ->
            val timeDbl = time.toDouble()
            when {
                keyframeJson is JsonObject -> {
                    // The interpolation type, at time of writing, is only set for catmullrom ("smooth" in Blockbench)
                    // while all other interpolation types, whether it's step or linear or bezier, work by just having
                    // one or many linearly interpolated frames in some fancy way that I don't understand. Doesn't matter
                    // though, once they write to a file they're just many little linear keyframes.
                    val interpolationType = when (keyframeJson.get("lerp_mode")?.asString ?: "linear") {
                        "catmullrom" -> InterpolationType.SMOOTH
                        else -> InterpolationType.LINEAR
                    }
                    if (keyframeJson.has("post")) {
                        val post = keyframeJson["post"]
                        keyframes[timeDbl] = JumpBedrockAnimationKeyFrame(
                            time = timeDbl,
                            transformation = transformation,
                            pre = deserializeMolangBoneValue(keyframeJson["pre"]?.asJsonArray ?: post.asJsonArray, transformation),
                            post = deserializeMolangBoneValue(post.asJsonArray, transformation),
                            interpolationType = interpolationType
                        )
                    } else if (keyframeJson.has("pre")) {
                        val pre = keyframeJson["pre"]
                        keyframes[timeDbl] = JumpBedrockAnimationKeyFrame(
                            time = timeDbl,
                            transformation = transformation,
                            pre = deserializeMolangBoneValue(pre.asJsonArray, transformation),
                            post = deserializeMolangBoneValue(keyframeJson["post"]?.asJsonArray ?: pre.asJsonArray, transformation),
                            interpolationType = interpolationType
                        )
                    } else {
                        throw IllegalStateException("transformation data ('post') could not be found")
                    }
                }
                keyframeJson is JsonArray -> {
//                    val transformationData = keyframeJson.map { it.asDouble }
//                    val transformationVector = Vec3d(transformationData[0], transformationData[1], transformationData[2])
                    keyframes[timeDbl] = SimpleBedrockAnimationKeyFrame(
                        time = timeDbl,
                        transformation = transformation,
                        data = deserializeMolangBoneValue(keyframeJson, transformation),
                        interpolationType = InterpolationType.LINEAR
                    )
                }
                else -> throw IllegalStateException("keyframe json could not be parsed")
            }
        }
        return keyframes
    }

}