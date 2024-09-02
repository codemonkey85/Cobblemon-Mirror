/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.util.effectiveName
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.resolveDouble
import net.minecraft.CrashReport
import net.minecraft.ReportedException
import net.minecraft.client.Minecraft
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.*

data class BedrockAnimationGroup(
    val formatVersion: String,
    val animations: Map<String, BedrockAnimation>
)

abstract class BedrockEffectKeyframe(val seconds: Float) {
    abstract fun run(entity: Entity, state: PosableState)
}

class BedrockParticleKeyframe(
    seconds: Float,
    val effect: BedrockParticleOptions,
    val locator: String,
    val scripts: List<Expression>
) : BedrockEffectKeyframe(seconds) {
    fun isSameAs(other: BedrockParticleKeyframe): Boolean {
        return if (seconds != other.seconds) {
            false
        } else if (effect != other.effect) {
            false
        } else if (locator != other.locator) {
            false
        } else if (scripts.map { it.getString() }.toSet() != other.scripts.map { it.getString() }.toSet()) {
            false
        } else {
            true
        }
    }

    override fun run(entity: Entity, state: PosableState) {
        val world = entity.level() as? ClientLevel ?: return
        val matrixWrapper = state.locatorStates[locator] ?: state.locatorStates["root"]!!

        if (this in state.poseParticles) {
            return
        }

        val particleRuntime = MoLangRuntime()

        // Share the query struct from the entity so the particle can query entity properties
        particleRuntime.environment.query = state.runtime.environment.query

        val storm = ParticleStorm(
            effect = effect,
            matrixWrapper = matrixWrapper,
            world = world,
            runtime = particleRuntime,
            sourceVelocity = { entity.deltaMovement },
            sourceAlive = { !entity.isRemoved && this in state.poseParticles },
            sourceVisible = { !entity.isInvisible },
            entity = entity,
            onDespawn = { state.poseParticles.remove(this) }
        )

        state.poseParticles.add(this)
        storm.runtime.execute(this.scripts)
        storm.spawn()
    }
}

class BedrockSoundKeyframe(
    seconds: Float,
    val sound: ResourceLocation
): BedrockEffectKeyframe(seconds) {
    override fun run(entity: Entity, state: PosableState) {
        val soundEvent = SoundEvent.createVariableRangeEvent(sound) // Means we don't need to setup a sound registry entry for every single thing
        if (soundEvent != null) {
            Minecraft.getInstance().soundManager.play(
                SimpleSoundInstance(
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1F,
                    1F,
                    entity.level().random,
                    entity.x,
                    entity.y,
                    entity.z
                )
            )
        }
    }
}

class BedrockInstructionKeyframe(
    seconds: Float,
    val expressions: ExpressionLike
): BedrockEffectKeyframe(seconds) {
    override fun run(entity: Entity, state: PosableState) {
        expressions.resolve(state.runtime)
    }
}

data class BedrockAnimation(
    val shouldLoop: Boolean,
    val animationLength: Double,
    val effects: List<BedrockEffectKeyframe>,
    val boneTimelines: Map<String, BedrockBoneTimeline>
) {
    /** Useful to have, gets set after loading the animation. */
    var name: String = ""

    fun run(context: RenderContext, model: PosableModel, state: PosableState, animationSeconds: Float, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, intensity: Float): Boolean {
        var animationSeconds = animationSeconds
        if (shouldLoop) {
            animationSeconds %= animationLength.toFloat()
        } else if (animationSeconds > animationLength && animationLength > 0) {
            return false
        }

        val runtime = state.runtime

        runtime.environment.setSimpleVariable("limb_swing", DoubleValue(limbSwing.toDouble()))
        runtime.environment.setSimpleVariable("limb_swing_amount", DoubleValue(limbSwingAmount.toDouble()))
        runtime.environment.setSimpleVariable("age_in_ticks", DoubleValue(ageInTicks.toDouble()))

        boneTimelines.forEach { (boneName, timeline) ->
            val part = model.relevantPartsByName[boneName] ?: if (boneName == "root_part") (model.rootPart as ModelPart) else null
            if (part !== null) {
                if (!timeline.position.isEmpty()) {
                    val position = timeline.position.resolve(animationSeconds.toDouble(), runtime).scale(intensity.toDouble())
                    part.apply {
                        x += position.x.toFloat()
                        y += position.y.toFloat()
                        z += position.z.toFloat()
                    }
                }

                if (!timeline.rotation.isEmpty()) {
                    try {
                        val rotation = timeline.rotation.resolve(animationSeconds.toDouble(), runtime).scale(intensity.toDouble())
                        part.apply {
                            xRot += rotation.x.toFloat().toRadians()
                            yRot += rotation.y.toFloat().toRadians()
                            zRot += rotation.z.toFloat().toRadians()
                        }
                    } catch (e: Exception) {
                        val exception = IllegalStateException("Bad animation for entity: ${(model.context.request(RenderContext.ENTITY))!!.effectiveName().string}", e)
                        val crash = CrashReport("Cobblemon encountered an unexpected crash", exception)
                        val section = crash.addCategory("Animation Details")
                        section.setDetail("Pose", state.currentPose!!)
                        section.setDetail("Bone", boneName)

                        throw ReportedException(crash)
                    }
                }

                if (!timeline.scale.isEmpty()) {
                    var scale = timeline.scale.resolve(animationSeconds.toDouble(), runtime)
                    // If the goal is to make the invisible then kick that into gear after 0.5. Maybe could work better somehow else.
                    if (scale == Vec3.ZERO && intensity > 0.5) {
                        part.xScale *= scale.x.toFloat()
                        part.yScale *= scale.y.toFloat()
                        part.zScale *= scale.z.toFloat()
                    } else {
                        // The deviation from 1 is what we want to multiply by the intensity of the animation.
                        val deviation = scale.scale(-1.0).add(1.0, 1.0, 1.0)
                        val weakenedDeviation = deviation.scale(intensity.toDouble())
                        scale = weakenedDeviation.subtract(1.0, 1.0, 1.0).scale(-1.0)
                        part.xScale *= scale.x.toFloat()
                        part.yScale *= scale.y.toFloat()
                        part.zScale *= scale.z.toFloat()
                    }
                }
            }
        }
        return true
    }

    fun applyEffects(entity: Entity, state: PosableState, previousSeconds: Float, newSeconds: Float) {
        val effectCondition: (effectKeyframe: BedrockEffectKeyframe) -> Boolean =
            if (previousSeconds > newSeconds) {
                { it.seconds >= previousSeconds || it.seconds <= newSeconds }
            } else {
                { it.seconds in previousSeconds..newSeconds }
            }

        effects.filter(effectCondition).forEach { it.run(entity, state) }
    }
}

interface BedrockBoneValue {
    fun resolve(time: Double, runtime: MoLangRuntime): Vec3
    fun isEmpty(): Boolean
}

object EmptyBoneValue : BedrockBoneValue {
    override fun resolve(time: Double, runtime: MoLangRuntime) = Vec3.ZERO
    override fun isEmpty() = true
}

data class BedrockBoneTimeline (
    val position: BedrockBoneValue,
    val rotation: BedrockBoneValue,
    val scale: BedrockBoneValue
)
class MolangBoneValue(
    val x: Expression,
    val y: Expression,
    val z: Expression,
    transformation: Transformation
) : BedrockBoneValue {
    val yMul = if (transformation == Transformation.POSITION) -1 else 1
    override fun isEmpty() = false
    override fun resolve(time: Double, runtime: MoLangRuntime): Vec3 {
        val environment = runtime.environment
        environment.setSimpleVariable("anim_time", DoubleValue(time))
        environment.setSimpleVariable("camera_rotation_x", DoubleValue(Minecraft.getInstance().gameRenderer.mainCamera.rotation().x.toDouble()))
        environment.setSimpleVariable("camera_rotation_y", DoubleValue(Minecraft.getInstance().gameRenderer.mainCamera.rotation().y.toDouble()))
        return Vec3(
            runtime.resolveDouble(x),
            runtime.resolveDouble(y) * yMul,
            runtime.resolveDouble(z)
        )
    }

}
class BedrockKeyFrameBoneValue : TreeMap<Double, BedrockAnimationKeyFrame>(), BedrockBoneValue {
    fun SortedMap<Double, BedrockAnimationKeyFrame>.getAtIndex(index: Int?): BedrockAnimationKeyFrame? {
        if (index == null) return null
        val key = this.keys.elementAtOrNull(index)
        return if (key != null) this[key] else null
    }

    override fun resolve(time: Double, runtime: MoLangRuntime): Vec3 {
        var afterIndex : Int? = keys.indexOfFirst { it > time }
        if (afterIndex == -1) afterIndex = null
        val beforeIndex = when (afterIndex) {
            null -> size - 1
            0 -> null
            else -> afterIndex - 1
        }
        val after = getAtIndex(afterIndex)
        val before = getAtIndex(beforeIndex)

        val afterData = after?.pre?.resolve(time, runtime) ?: Vec3.ZERO
        val beforeData = before?.post?.resolve(time, runtime) ?: Vec3.ZERO

        if (before != null || after != null) {
            if (before != null && before.interpolationType == InterpolationType.SMOOTH || after != null && after.interpolationType == InterpolationType.SMOOTH) {
                when {
                    before != null && after != null -> {
                        val beforePlusIndex = if (beforeIndex == null || beforeIndex == 0) null else beforeIndex - 1
                        val beforePlus = getAtIndex(beforePlusIndex)
                        val afterPlusIndex = if (afterIndex == null || afterIndex == size - 1) null else afterIndex + 1
                        val afterPlus = getAtIndex(afterPlusIndex)
                        return catmullromLerp(beforePlus, before, after, afterPlus, time, runtime)
                    }
                    before != null -> return beforeData
                    else -> return afterData
                }
            }
            else {
                when {
                    before != null && after != null -> {
                        return Vec3(
                            beforeData.x + (afterData.x - beforeData.x) * linearLerpAlpha(
                                before.time,
                                after.time,
                                time
                            ),
                            beforeData.y + (afterData.y - beforeData.y) * linearLerpAlpha(
                                before.time,
                                after.time,
                                time
                            ),
                            beforeData.z + (afterData.z - beforeData.z) * linearLerpAlpha(before.time, after.time, time)
                        )
                    }
                    before != null -> return beforeData
                    else -> return afterData
                }
            }
        }
        else {
            return Vec3(0.0, 0.0, 0.0)
        }
    }

}

abstract class BedrockAnimationKeyFrame(
    val time: Double,
    val transformation: Transformation,
    val interpolationType: InterpolationType
) {
    abstract val pre: MolangBoneValue
    abstract val post: MolangBoneValue
}

class SimpleBedrockAnimationKeyFrame(
    time: Double,
    transformation: Transformation,
    interpolationType: InterpolationType,
    val data: MolangBoneValue
): BedrockAnimationKeyFrame(time, transformation, interpolationType) {
    override val pre = data
    override val post = data
}

class JumpBedrockAnimationKeyFrame(
    time: Double,
    transformation: Transformation,
    interpolationType: InterpolationType,
    override val pre: MolangBoneValue,
    override val post: MolangBoneValue
): BedrockAnimationKeyFrame(time, transformation, interpolationType)

enum class InterpolationType {
    SMOOTH, LINEAR
}

enum class Transformation {
    POSITION, ROTATION, SCALE
}