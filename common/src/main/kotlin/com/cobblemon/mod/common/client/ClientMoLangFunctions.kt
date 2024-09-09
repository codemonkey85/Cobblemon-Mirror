/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.struct.ArrayStruct
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.render.models.blockbench.ExcludedLabels
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.animation.*
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.getDoubleOrNull
import com.cobblemon.mod.common.util.getStringOrNull
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import java.util.function.Function
object ClientMoLangFunctions {
    val clientFunctions = hashMapOf<String, Function<MoParams, Any>>(
        "sound" to java.util.function.Function { params ->
            if (params.get<MoValue>(0) !is StringValue) {
                return@Function Unit
            }
            val soundEvent = SoundEvent.createVariableRangeEvent(params.getString(0).asIdentifierDefaultingNamespace())
            if (soundEvent != null) {
                val pitch = if (params.contains(2)) params.getDouble(2).toFloat() else 1F
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, pitch))
            }
        },
        "is_time" to java.util.function.Function { params ->
            val time = (Minecraft.getInstance().level?.dayTime() ?: 0) % 24000
            val min = params.getInt(0)
            val max = params.getInt(1)
            time in min..max
        },
        "say" to java.util.function.Function { params -> Minecraft.getInstance().player?.sendSystemMessage(params.getString(0).text()) ?: Unit },
    )

    val animationFunctions = hashMapOf<String, Function<PosableModel, Function<MoParams, Any>>>(
        "exclude_labels" to Function { model -> Function { params ->
                val labels = params.params.map { it.asString() }
                ObjectValue(ExcludedLabels(labels))
            }
        },
        "bedrock_primary" to Function {
            model -> Function { params ->
                val group = params.getString(0)
                val animation = params.getString(1)
                val anim = model.bedrockStateful(group, animation)
                val excludedLabels = mutableSetOf<String>()
                var curve: WaveFunction = { t ->
                    if (t < 0.1) {
                        t * 10
                    } else if (t < 0.9) {
                        1F
                    } else {
                        1F
                    }
                }
                for (index in 2 until params.params.size) {
                    val param = params.get<MoValue>(index)
                    if (param is ObjectValue<*>) {
                        val obj = param.obj
                        if (obj is ExcludedLabels) {
                            excludedLabels.addAll(obj.labels)
                        } else {
                            curve = param.obj as WaveFunction
                        }
                        continue
                    }

                    val label = params.getString(index) ?: continue
                    excludedLabels.add(label)
                }

                ObjectValue(
                    PrimaryAnimation(
                        animation = anim,
                        excludedLabels = excludedLabels,
                        curve = curve
                    )
                )
            }
        },
        "bedrock_stateful" to Function {
            model -> Function { params ->
                val group = params.getString(0)
                val animation = params.getString(1)
                val anim = model.bedrockStateful(group, animation)
                val enduresPrimary = "endures_primary_animations" in params.params.mapNotNull { it.asString() }
                anim.enduresPrimaryAnimations = enduresPrimary
                ObjectValue(anim)
            }
        },
        "bedrock" to Function {
            model -> Function { params ->
                val group = params.getString(0)
                val animation = params.getString(1)
                val anim = model.bedrock(group, animation)
                ObjectValue(anim)
            }
        },
        "look" to Function {
            model -> Function { params ->
                val boneName = params.getString(0)
                val pitchMultiplier = params.getDoubleOrNull(1) ?: 1F
                val yawMultiplier = params.getDoubleOrNull(2) ?: 1F
                val maxPitch = params.getDoubleOrNull(3) ?: 70F
                val minPitch = params.getDoubleOrNull(4) ?: -45F
                val maxYaw = params.getDoubleOrNull(5) ?: 45F
                ObjectValue(
                    SingleBoneLookAnimation(
                        bone = model.getPart(boneName),
                        pitchMultiplier = pitchMultiplier.toFloat(),
                        yawMultiplier = yawMultiplier.toFloat(),
                        maxPitch = maxPitch.toFloat(),
                        minPitch = minPitch.toFloat(),
                        maxYaw = maxYaw.toFloat()
                    )
                )
            }
        },
        "quadruped_walk" to Function {
            model -> Function { params ->
                val periodMultiplier = params.getDoubleOrNull(0) ?: 0.6662F

                val amplitudeMultiplier = params.getDoubleOrNull(1) ?: 1.4F
                val leftFrontLeftName = params.getStringOrNull(2) ?: "leg_front_left"
                val leftFrontRightName = params.getStringOrNull(3) ?: "leg_front_right"
                val leftBackLeftName = params.getStringOrNull(4) ?: "leg_back_left"
                val leftBackRightName = params.getStringOrNull(5) ?: "leg_back_right"

                ObjectValue(
                    QuadrupedWalkAnimation(
                        periodMultiplier = periodMultiplier.toFloat(),
                        amplitudeMultiplier = amplitudeMultiplier.toFloat(),
                        legFrontLeft = model.getPart(leftFrontLeftName),
                        legFrontRight = model.getPart(leftFrontRightName),
                        legBackLeft = model.getPart(leftBackLeftName),
                        legBackRight = model.getPart(leftBackRightName)
                    )
                )
            }
        },
        "biped_walk" to Function {
            model -> Function { params ->
                val periodMultiplier = params.getDoubleOrNull(0) ?: 0.6662F
                val amplitudeMultiplier = params.getDoubleOrNull(1) ?: 1.4F
                val leftLegName = params.getStringOrNull(2) ?: "leg_left"
                val rightLegName = params.getStringOrNull(3) ?: "leg_right"

                ObjectValue(
                    BipedWalkAnimation(
                        periodMultiplier = periodMultiplier.toFloat(),
                        amplitudeMultiplier = amplitudeMultiplier.toFloat(),
                        leftLeg = model.getPart(leftLegName),
                        rightLeg = model.getPart(rightLegName)
                    )
                )
            }
        },
        "bimanual_swing" to Function {
            model -> Function { params ->
                val swingPeriodMultiplier = params.getDoubleOrNull(0) ?: 0.6662F
                val amplitudeMultiplier = params.getDoubleOrNull(1) ?: 1F
                val leftArmName = params.getStringOrNull(2) ?: "arm_left"
                val rightArmName = params.getStringOrNull(3) ?: "arm_right"

                ObjectValue(
                    BimanualSwingAnimation(
                        swingPeriodMultiplier = swingPeriodMultiplier.toFloat(),
                        amplitudeMultiplier = amplitudeMultiplier.toFloat(),
                        leftArm = model.getPart(leftArmName),
                        rightArm = model.getPart(rightArmName)
                    )
                )
            }
        },
        "sine_wing_flap" to Function {
            model -> Function { params ->
//                    val verticalShift = (-14F).toRadians(), val period = 0.9F, amplitude = 0.9F
                val amplitude = params.getDoubleOrNull(0) ?: 0.9F
                val period = params.getDoubleOrNull(1) ?: 0.9F
                val verticalShift = params.getDoubleOrNull(2) ?: 0F
                val axis = params.getStringOrNull(3) ?: "y"
                val axisIndex = when (axis) {
                    "x" -> ModelPartTransformation.X_AXIS
                    "y" -> ModelPartTransformation.Y_AXIS
                    "z" -> ModelPartTransformation.Z_AXIS
                    else -> ModelPartTransformation.Y_AXIS
                }
                val wingLeft = params.getStringOrNull(4) ?: "wing_left"
                val wingRight = params.getStringOrNull(5) ?: "wing_right"

                ObjectValue(
                    WingFlapIdleAnimation(
                        rotation = sineFunction(
                            verticalShift = verticalShift.toFloat(),
                            period = period.toFloat(),
                            amplitude = amplitude.toFloat()
                        ),
                        axis = axisIndex,
                        leftWing = model.getPart(wingLeft),
                        rightWing = model.getPart(wingRight)
                    )
                )
            }
        },
        "bedrock_quirk" to Function {
            model -> Function { params ->
                val animationGroup = params.getString(0)
                val animationNames = params.get<MoValue>(1)
                    ?.let { if (it is ArrayStruct) it.map.values.map { it.asString() } else listOf(it.asString()) }
                    ?: listOf()
                val minSeconds = params.getDoubleOrNull(2) ?: 8F
                val maxSeconds = params.getDoubleOrNull(3) ?: 30F
                val loopTimes = params.getDoubleOrNull(4)?.toInt() ?: 1
                ObjectValue(
                    model.quirk(
                        secondsBetweenOccurrences = minSeconds.toFloat() to maxSeconds.toFloat(),
                        condition = { true },
                        loopTimes = 1..loopTimes,
                        animation = { model.bedrockStateful(animationGroup, animationNames.random()) }
                    )
                )
            }
        },
        "bedrock_primary_quirk" to Function { model ->
            Function { params ->
                val animationGroup = params.getString(0)
                val animationNames = params.get<MoValue>(1)
                    ?.let { if (it is ArrayStruct) it.map.values.map { it.asString() } else listOf(it.asString()) }
                    ?: listOf()
                val minSeconds = params.getDoubleOrNull(2) ?: 8F
                val maxSeconds = params.getDoubleOrNull(3) ?: 30F
                val loopTimes = params.getDoubleOrNull(4)?.toInt() ?: 1
                val excludedLabels = mutableSetOf<String>()
                var curve: WaveFunction = { t ->
                    if (t < 0.1) {
                        t * 10
                    } else if (t < 0.9) {
                        1F
                    } else {
                        1F
                    }
                }
                for (index in 5 until params.params.size) {
                    val param = params.get<MoValue>(index)
                    if (param is ObjectValue<*>) {
                        curve = param.obj as WaveFunction
                        continue
                    }

                    val label = params.getString(index) ?: continue
                    excludedLabels.add(label)
                }
                ObjectValue(
                    model.quirk(
                        secondsBetweenOccurrences = minSeconds.toFloat() to maxSeconds.toFloat(),
                        condition = { true },
                        loopTimes = 1..loopTimes,
                        animation = {
                            PrimaryAnimation(
                                model.bedrockStateful(animationGroup, animationNames.random()),
                                excludedLabels = excludedLabels,
                                curve = curve
                            )
                        }
                    )
                )
            }
        }
    )

    fun PosableModel.animationFunctions(): HashMap<String, Function<MoParams, Any>> = animationFunctions.map { (k, v) -> k to v.apply(this) }.toHashMap()

    fun MoLangRuntime.setupClient(): MoLangRuntime {
        environment.query.addFunctions(clientFunctions)
        return this
    }

    private fun <K, V, P : Pair<K, V>> List<Pair<K, V>>.toHashMap(): HashMap<K, V> {
        val map = hashMapOf<K, V>()
        map.putAll(this.toMap())
        return map
    }
}


