/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.ArrayStruct
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockParticleKeyframe
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockPoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.QuirkData
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Represents some kind of animation state for an entity or GUI element or other renderable component in the game.
 * This state object is responsible for maintaining the current pose, current active animations, any quirks that
 * are in effect, and the [MoLangRuntime] to use for its animations. These states can be effectively mocked using
 * [FloatingState].
 *
 * This class also represents a [Schedulable] frame of reference. The core contract of a PosableState is that it is
 * going to have a central timing mechanism that is moving forward with ticking or rendering or both. This is seen
 * with the [age] and [currentPartialTicks] properties. You don't necessarily have to use both as GUIs will rely
 * entirely on partial ticks and endlessly increase them without resetting. [animationSeconds] is derived from the
 * combination of these two properties.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
abstract class PosableState : Schedulable {
    var currentModel: PosableModel? = null
        set(value) {
            val changed = field != value
            field = value
            if (value != null && changed) {
                val entity = getEntity() as? PosableEntity ?: return
                entity.struct.addFunctions(value.functions.functions)
                runtime.environment.query.addFunctions(value.functions.functions)
                // Locators need to be initialized asap, even if they aren't in perfect positions. The reason for this
                // is that the locators might be called upon by frame 0 particle effects and if they aren't defined
                // it'll crash. For non-entity states we don't give a shit though.
                value.updateLocators(getEntity() ?: return, this)
            }
        }
    /** The name of the current pose. */
    var currentPose: String? = null
    /** A kind of cache for the current aspects being rendered. It's a bit sloppily maintained but necessary. */
    var currentAspects: Set<String> = emptySet()
    /** The currently active [PrimaryAnimation], if there is one. */
    var primaryAnimation: PrimaryAnimation? = null
    /** All of the current [ActiveAnimation]. */
    val activeAnimations: MutableList<ActiveAnimation> = mutableListOf()
    /** Any active [ModelQuirk]s and their [QuirkData]. */
    val quirks = mutableMapOf<ModelQuirk<*>, QuirkData>()
    /**
     * Any particle effect keyframes that are generated from a [Pose] and area active right now. This prevents duplicates
     * for animations that loop and have particle keyframes at some point in time on that loop.
     */
    val poseParticles = mutableListOf<BedrockParticleKeyframe>()

    /** A simple getter to pull together all non-pose animations that are non-primary. */
    val allActiveAnimations: List<ActiveAnimation> get() = activeAnimations + quirks.flatMap { it.value.animations }

    /** The tick-driven age of the state. Combines with [currentPartialTicks] to produce the animation seconds. */
    protected var age = 0
    /**
     * The partial tick value of the state. Combines with [age] to produce the animation seconds. For entity-based state
     * this will be consistently reset to a new value, whereas non-entity-based state will increment this value.
     *
     * See [FloatingState].
     */
    protected var currentPartialTicks = 0F

    /** The current intensity to be used for [Pose] animations. */
    var poseIntensity = 1F

    /** The derived animation seconds. It is [age] + [currentPartialTicks], converted to seconds. */
    val animationSeconds: Float get() = (age + getPartialTicks()) / 20F

    /**
     * The current positions of all locators. The positions are stored as full [MatrixWrapper]s so it is not only the
     * position but the local space.
     */
    val locatorStates = mutableMapOf<String, MatrixWrapper>()

    /** A set of actions that should occur soon, on the render thread. I doubt this will be needed long term. */
    val renderQueue = ConcurrentLinkedQueue<() -> Unit>()

    /** This gets called 500 million times so use a mutable value for runtime */
    private val reusableAnimTime = DoubleValue(0.0)

    /** All of the MoLang functions that can be applied to something with this state. */
    val functions = QueryStruct(hashMapOf())
        .addFunction("anim_time") {
            reusableAnimTime.value = animationSeconds.toDouble()
            reusableAnimTime
        }
        .addFunction("current_aspects") { ArrayStruct(currentAspects.mapIndexed { index, s -> "$index" to StringValue(s)}.toMap())}
        .addFunction("has_aspect") { params -> DoubleValue(params.get<MoValue>(0).asString() in currentAspects) }
        .addFunction("has_entity") { DoubleValue(getEntity() != null) }
        .addFunction("pose") { StringValue(currentPose ?: "") }
        .addFunction("sound") { params ->
            val entity = getEntity() ?: return@addFunction Unit
            if (params.get<MoValue>(0) !is StringValue) {
                return@addFunction Unit
            }
            val soundEvent = SoundEvent.createVariableRangeEvent(params.getString(0).asIdentifierDefaultingNamespace())
            if (soundEvent != null) {
                val volume = if (params.contains(1)) params.getDouble(1).toFloat() else 1F
                val pitch = if (params.contains(2)) params.getDouble(2).toFloat() else 1F
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance(soundEvent, SoundSource.NEUTRAL, volume, pitch, entity.level().random, entity.x, entity.y, entity.z)
                )
            }
        }
        .addFunction("play_animation") { params ->
            val animationParameter = params.get<MoValue>(0)
            val animation = if (animationParameter is ObjectValue<*>) {
                animationParameter.obj as BedrockActiveAnimation
            } else {
                currentModel?.getAnimation(this, animationParameter.asString(), runtime)
            }
            if (animation != null) {
                if (animation is PrimaryAnimation) {
                    addPrimaryAnimation(animation)
                } else {
                    addActiveAnimation(animation)
                }
            }
            return@addFunction Unit
        }
        .addFunction("particle") { params ->
            val particlesParam = params.get<MoValue>(0)
            val particles = mutableListOf<String>()
            when (particlesParam) {
                is StringValue -> particles.add(particlesParam.value)
                is VariableStruct -> particles.addAll(particlesParam.map.values.map { it.asString() })
                else -> return@addFunction Unit
            }

            val effectIds = particles.map { it.asIdentifierDefaultingNamespace() }
            for (effectId in effectIds) {
                val locator = if (params.params.size > 1) params.getString(1) else "root"
                val effect = BedrockParticleOptionsRepository.getEffect(effectId) ?: run {
                    LOGGER.error("Unable to find a particle effect with id $effectId")
                    return@addFunction Unit
                }

                val entity = getEntity() ?: return@addFunction Unit
                val world = entity.level() as ClientLevel
                val matrixWrapper = locatorStates[locator] ?: locatorStates["root"]!!

                val particleRuntime = MoLangRuntime().setup().setupClient()
                particleRuntime.environment.query.addFunction("entity") { runtime.environment.query }

                    val storm = ParticleStorm(
                        effect = effect,
                        entity = entity,
                        matrixWrapper = matrixWrapper,
                        world = world,
                        runtime = particleRuntime,
                        sourceVelocity = { entity.deltaMovement },
                        sourceAlive = { !entity.isRemoved },
                        sourceVisible = { !entity.isInvisible }
                    )

                storm.spawn()
            }
        }

    val runtime: MoLangRuntime = MoLangRuntime().setup().setupClient().also {
        it.environment.query.addFunctions(functions.functions)
    }

    /** Gets the entity related to this state if this state is actually attached to an entity. */
    abstract fun getEntity(): Entity?
    fun getPartialTicks() = currentPartialTicks
    open fun updateAge(age: Int) {
        this.age = age
    }

    /**
     * Performs some ticking operation with an entity. This will apply a lot of logic that occurs each client tick
     * and that needs an entity. This includes updating locators, checking for any primary animation expiry, applying
     * particle effects for any active animation, and updating [age].
     */
    open fun incrementAge(entity: Entity) {
        val previousAge = age
        updateAge(age + 1)
        currentModel?.let {
            updateLocatorPosition(entity.position())
            it.validatePose(entity as? PosableEntity, this)
        }
        runEffects(entity, previousAge, age)
        val primaryAnimation = primaryAnimation ?: return
        if (primaryAnimation.started + primaryAnimation.duration <= animationSeconds) {
            this.primaryAnimation = null
            primaryAnimation.afterAction.accept(Unit)
        }
    }

    /** Decides how an update to partial ticks should be applied to the state. See [FloatingState] for how it could happen. */
    abstract fun updatePartialTicks(partialTicks: Float)

    /** Can be used to reset the animation timer for poses that don't transition nicely. Bit of an afterthought solution.  */
    open fun reset() {
        updateAge(0)
    }

    /**
     * Scans through the set of animations provided and begins playing the first one that is registered
     * on the entity. The goal is to have most-specific animations first and more generic ones last, so
     * where detailed animations exist they will be used and where they don't there is still a fallback.
     *
     * E.g. ['thunderbolt', 'electric', 'special']
     */
    fun addFirstAnimation(animation: Set<String>) {
        val model = currentModel ?: return
        val animation = animation.firstNotNullOfOrNull { model.getAnimation(this, it, runtime) } ?: return
        if (animation is PrimaryAnimation) {
            addPrimaryAnimation(animation)
        } else {
            addActiveAnimation(animation)
        }
    }

    fun isPosedIn(vararg poses: Pose) = poses.any { it.poseName == currentPose }
    fun isNotPosedIn(vararg poses: Pose) = poses.none { it.poseName == currentPose }

    fun preRender() {
        while (renderQueue.peek() != null) {
            val action = renderQueue.poll()
            action()
        }
    }

    fun doLater(action: () -> Unit) {
        renderQueue.offer(action)
    }

    fun setPoseToFirstSuitable(poseType: PoseType? = null) {
        val model = currentModel ?: return
        val pose = model.getFirstSuitablePose(this, poseType)
        if (pose.poseName == this.currentPose) {
            return
        }
        setPose(pose.poseName)
    }

    /**
     * Changes the current pose over to the one mentioned by name. This relies on [currentModel] being non-null.
     *
     * As part of this process there is some juggling to deal with particle effects so that if they exist on both
     * pose's animations and the current pose's animations, they are not duplicated. This is done by removing any
     * particle effects that are not unique to the old pose. We also immediately run the particle effects that are
     * on the new pose and are set to run at the start of the animation.
     *
     * It's honestly quite jank but I'm not sure how Bedrock would even go about solving this.
     */
    fun setPose(pose: String) {
        currentPose = pose
        poseIntensity = 1F
        val model = currentModel
        if (model != null) {
            val poseImpl = model.poses[pose] ?: return
            poseParticles.removeIf { particle -> poseImpl.animations.filterIsInstance<BedrockPoseAnimation>().flatMap { it.particleKeyFrames }.none(particle::isSameAs) }
            poseImpl.onTransitionedInto(this)
            val entity = getEntity()
            if (entity != null) {
                poseImpl.animations
                    .filterIsInstance<BedrockPoseAnimation>()
                    .flatMap { it.particleKeyFrames }
                    .filter { particle -> particle.seconds == 0F && poseParticles.none(particle::isSameAs) }
                    .forEach { it.run(entity, this) }
            }
        }
    }

    fun setActiveAnimations(vararg animations: ActiveAnimation) {
        activeAnimations.clear()
        activeAnimations.addAll(animations)
    }

    /**
     * Updates the base position of all the locators. Doesn't require the model.
     */
    fun updateLocatorPosition(position: Vec3) {
        locatorStates.values.toList().forEach { it.updatePosition(position) }
    }

    fun addActiveAnimation(animation: ActiveAnimation, whenComplete: (state: PosableState) -> Unit = {}) {
        this.activeAnimations.add(animation)
        val duration = animation.duration
        if (duration > 0F) {
            after(seconds = (duration * 20F).toInt() / 20F) {
                whenComplete(this)
            }
        }
    }

    /**
     * Sets the primary animation and dumps any active animations.
     */
    fun addPrimaryAnimation(primaryAnimation: PrimaryAnimation) {
        this.primaryAnimation = primaryAnimation
        this.activeAnimations.clear()
        this.quirks.clear()
        this.poseIntensity = 1F
        primaryAnimation.started = animationSeconds
    }

    /**
     * Runs any effects that are associated with the current state of the entity. This includes running effects for
     * all active animations, the primary animation, and any pose animations that are relevant.
     */
    fun runEffects(entity: Entity, previousAge: Int, newAge: Int) {
        val previousSeconds = previousAge / 20F
        val currentSeconds = newAge / 20F
        allActiveAnimations.forEach { it.applyEffects(entity, this, previousSeconds, currentSeconds) }
        primaryAnimation?.animation?.applyEffects(entity, this, previousSeconds, currentSeconds)
        currentModel?.let { model ->
            val pose = currentPose?.let { model.poses[it] }
            // Effects start playing from pose animations as long as the intensity is above 0.5. Pretty sloppy honestly.
            pose?.animations
                ?.filter { shouldIdleRun(it, 0.5F) && it.condition(this) }
                ?.forEach { it.applyEffects(entity, this, previousSeconds, currentSeconds) }
        }
    }

    /** Checks if the current primary animation interferes with the given pose animation and is below a threshold intensity. */
    fun shouldIdleRun(poseAnimation: PoseAnimation, requiredIntensity: Float): Boolean {
        val primaryAnimation = primaryAnimation
        return if (primaryAnimation != null) {
            !primaryAnimation.prevents(poseAnimation) || this.poseIntensity > requiredIntensity
        } else {
            true
        }
    }

    /** Gets the appropriate intensity for the given pose animation, given the current primary animation. */
    fun getIdleIntensity(poseAnimation: PoseAnimation): Float {
        val primaryAnimation = primaryAnimation
        return if (primaryAnimation != null && primaryAnimation.prevents(poseAnimation)) {
            this.poseIntensity
        } else {
            1F
        }
    }
}