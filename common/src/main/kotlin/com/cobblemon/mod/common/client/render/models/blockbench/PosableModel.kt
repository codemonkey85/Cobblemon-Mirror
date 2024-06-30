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
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.ModelLayer
import com.cobblemon.mod.common.client.render.models.blockbench.animation.*
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockPoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.SimpleQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.getDoubleOrNull
import com.cobblemon.mod.common.util.getStringOrNull
import com.cobblemon.mod.common.util.plus
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d

/**
 * A model that can be posed and animated using [PoseAnimation]s and [ActiveAnimation]s. This
 * requires poses to be registered and should implement any [ModelFrame] interfaces that apply to this
 * model. This contains vast quantities of information about quirks, named animation references, locators,
 * portrait and profile translation and scaling, and default part positions.
 *
 * This is a singleton for a specific model. For example, an NPC with an unusually large head would be one
 * instance of this even if 100 NPCs are spawned with that model.
 *
 * Instantiations of a PosableModel can be the result of explicit coded subclasses or from JSON files.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
open class PosableModel(@Transient override val rootPart: Bone) : ModelFrame {
    @Transient
    lateinit var context: RenderContext

    /** Whether the renderer that will process this is going to do the weird -1.5 Y offset bullshit that the living entity renderer does. */
    @Transient
    open var isForLivingEntityRenderer = true

    var poses = mutableMapOf<String, Pose>()

    /** A way to view the definition of all the different locators that are registered for the model. */
    @Transient
    lateinit var locatorAccess: LocatorAccess

    open var portraitScale = 1F
    open var portraitTranslation = Vec3d(0.0, 0.0, 0.0)

    open var profileScale = 1F
    open var profileTranslation = Vec3d(0.0, 0.0, 0.0)

    var red = 1F
    var green = 1F
    var blue = 1F
    var alpha = 1F

    /** Named animations that can be referenced in generic named ways. This is different from the [PosableState] that stores the active animations. */
    val animations = mutableMapOf<String, ExpressionLike>()
    /** The definition of quirks that are possible for the model. This is different from the [PosableState] that stores the active quirk animations. */
    val quirks = mutableListOf<ModelQuirk<*>>()
    /**
     * A list of [ModelPartTransformation] that record the original joint positions, rotations, and scales of the model.
     * This allows the original state to be reset between renders.
     */
    @Transient
    val defaultPositions = mutableListOf<ModelPartTransformation>()
    /**
     * The named model parts that we expect will get modified. These are tracked so that they can be reset to their
     * original positions at the end of each render (reset using [setDefault]).
     */
    @Transient
    val relevantPartsByName = mutableMapOf<String, ModelPart>()

    /** Legacy faint code. */
    open fun getFaintAnimation(state: PosableState): ActiveAnimation? = null
    /** Legacy eating code. */
    open fun getEatAnimation(state: PosableState): ActiveAnimation? = null
    /** Legacy cry code. */
    @Transient
    open val cryAnimation: CryProvider = CryProvider { null }

    @Transient
    var currentLayers: Iterable<ModelLayer> = listOf()

    @Transient
    var bufferProvider: VertexConsumerProvider? = null

    @Transient
    var currentState: PosableState? = null

    @Transient
    val functions = QueryStruct(hashMapOf())
        .addFunction("exclude_labels") { params ->
            val labels = params.params.map { it.asString() }
            return@addFunction ObjectValue(ExcludedLabels(labels))
        }
        .addFunction("bedrock_primary") { params ->
            val group = params.getString(0)
            val animation = params.getString(1)
            val anim = bedrockStateful(group, animation)
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

            return@addFunction ObjectValue(
                PrimaryAnimation(
                    animation = anim,
                    excludedLabels = excludedLabels,
                    curve = curve
                )
            )
        }
        .addFunction("bedrock_stateful") { params ->
            val group = params.getString(0)
            val animation = params.getString(1)
            val anim = bedrockStateful(group, animation)
            return@addFunction ObjectValue(anim)
        }
        .addFunction("bedrock") { params ->
            val group = params.getString(0)
            val animation = params.getString(1)
            val anim = bedrock(group, animation)
            return@addFunction ObjectValue(anim)
        }
        .addFunction("look") { params ->
            val boneName = params.getString(0)
            val pitchMultiplier = params.getDoubleOrNull(1) ?: 1F
            val yawMultiplier = params.getDoubleOrNull(2) ?: 1F
            val maxPitch = params.getDoubleOrNull(3) ?: 70F
            val minPitch = params.getDoubleOrNull(4) ?: -45F
            val maxYaw = params.getDoubleOrNull(5) ?: 45F
            ObjectValue(
                SingleBoneLookAnimation(
                    bone = getPart(boneName),
                    pitchMultiplier = pitchMultiplier.toFloat(),
                    yawMultiplier = yawMultiplier.toFloat(),
                    maxPitch = maxPitch.toFloat(),
                    minPitch = minPitch.toFloat(),
                    maxYaw = maxYaw.toFloat()
                )
            )
        }
        .addFunction("quadruped_walk") { params ->
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
                    legFrontLeft = this.getPart(leftFrontLeftName),
                    legFrontRight = this.getPart(leftFrontRightName),
                    legBackLeft = this.getPart(leftBackLeftName),
                    legBackRight = this.getPart(leftBackRightName)
                )
            )
        }
        .addFunction("biped_walk") { params ->
            val periodMultiplier = params.getDoubleOrNull(0) ?: 0.6662F
            val amplitudeMultiplier = params.getDoubleOrNull(1) ?: 1.4F
            val leftLegName = params.getStringOrNull(2) ?: "leg_left"
            val rightLegName = params.getStringOrNull(3) ?: "leg_right"

            ObjectValue(
                BipedWalkAnimation(
                    periodMultiplier = periodMultiplier.toFloat(),
                    amplitudeMultiplier = amplitudeMultiplier.toFloat(),
                    leftLeg = this.getPart(leftLegName),
                    rightLeg = this.getPart(rightLegName)
                )
            )
        }
        .addFunction("bimanual_swing") { params ->
            val swingPeriodMultiplier = params.getDoubleOrNull(0) ?: 0.6662F
            val amplitudeMultiplier = params.getDoubleOrNull(1) ?: 1F
            val leftArmName = params.getStringOrNull(2) ?: "arm_left"
            val rightArmName = params.getStringOrNull(3) ?: "arm_right"

            ObjectValue(
                BimanualSwingAnimation(
                    swingPeriodMultiplier = swingPeriodMultiplier.toFloat(),
                    amplitudeMultiplier = amplitudeMultiplier.toFloat(),
                    leftArm = this.getPart(leftArmName),
                    rightArm = this.getPart(rightArmName)
                )
            )
        }
        .addFunction("sine_wing_flap") { params ->
            // verticalShift = -14F.toRadians(), period = 0.9F, amplitude = 0.9F
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
                    leftWing = this.getPart(wingLeft),
                    rightWing = this.getPart(wingRight)
                )
            )
        }
        .addFunction("bedrock_quirk") { params ->
            val animationGroup = params.getString(0)
            val animationNames = params.get<MoValue>(1)
                ?.let { if (it is ArrayStruct) it.map.values.map { it.asString() } else listOf(it.asString()) }
                ?: listOf()
            val minSeconds = params.getDoubleOrNull(2) ?: 8F
            val maxSeconds = params.getDoubleOrNull(3) ?: 30F
            val loopTimes = params.getDoubleOrNull(4)?.toInt() ?: 1
            ObjectValue(
                quirk(
                    secondsBetweenOccurrences = minSeconds.toFloat() to maxSeconds.toFloat(),
                    condition = { true },
                    loopTimes = 1..loopTimes,
                    animation = { bedrockStateful(animationGroup, animationNames.random()) }
                )
            )
        }
        .addFunction("bedrock_primary_quirk") { params ->
            val animationGroup = params.getString(0)
            val animationNames = params.get<MoValue>(1)?.let { if (it is ArrayStruct) it.map.values.map { it.asString() } else listOf(it.asString()) } ?: listOf()
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
                quirk(
                    secondsBetweenOccurrences = minSeconds.toFloat() to maxSeconds.toFloat(),
                    condition = { true },
                    loopTimes = 1..loopTimes,
                    animation = { PrimaryAnimation(bedrockStateful(animationGroup, animationNames.random()), excludedLabels = excludedLabels, curve = curve) }
                )
            )
        }

    @Transient
    val runtime = MoLangRuntime().setup().setupClient().also { it.environment.query.addFunctions(functions.functions) }

    /** Registers the different poses this model is capable of ahead of time. Should use [registerPose] religiously. */
    open fun registerPoses() {}

    /**
     * Generates an active animation by name. This can be legacy-backed cry or faint animations, a prepared builder
     * for an animation in the [animations] mapping, a product of MoLang in the name parameter, or a highly specific
     * format used in [extractAnimation].
     *
     * First priority is given to any named animations inside of [Pose], and then to the [animations] mapping, before
     * resorting to legacy, MoLang resolution, and finally the [extractAnimation] hail-Mary.
     */
    fun getAnimation(state: PosableState, name: String, runtime: MoLangRuntime): ActiveAnimation? {
        val poseAnimations = state.currentPose?.let(poses::get)?.namedAnimations ?: mapOf()
        val animation = resolveFromAnimationMap(poseAnimations, name, runtime)
            ?: resolveFromAnimationMap(animations, name, runtime)
            ?: when (name) {
                "cry" -> cryAnimation.invoke(state)
                "faint" -> getFaintAnimation(state)
                else -> {
                    try {
                        name.asExpressionLike().resolveObject(runtime).obj as ActiveAnimation
                    } catch (exception: Exception) {
                        extractAnimation(name)
                    }
                }
            }
        return animation
    }

    /**
     * Animation group : animation name [: primary]
     * e.g. "particle_dummy:animation.particle_dummy.dragon_claw_target:primary"
     * e.g. "particle_dummy:animation.particle.dummy.stat_up
     */
    fun extractAnimation(string: String): ActiveAnimation? {
        val group = string.substringBefore(":")
        val animationName = string.substringAfter(":").substringBefore(":")
        val isPrimary = string.endsWith(":primary")
        if (animationName.isNotBlank() && animationName != string) {
            val animation = BedrockAnimationRepository.tryGetAnimation(group, animationName) ?: return null
            return if (isPrimary) {
                PrimaryAnimation(BedrockActiveAnimation(animation))
            } else {
                BedrockActiveAnimation(animation)
            }
        } else {
            return null
        }
    }

    private fun resolveFromAnimationMap(
        map: Map<String, ExpressionLike>,
        name: String,
        runtime: MoLangRuntime
    ): ActiveAnimation? {
        val animationExpression = map[name] ?: return null
        return try {
            animationExpression.resolveObject(runtime).obj as ActiveAnimation
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Failed to create animation by name $name, most likely something wrong in the MoLang")
            e.printStackTrace()
            null
        }
    }

    fun withLayerContext(
        buffer: VertexConsumerProvider,
        state: PosableState,
        layers: Iterable<ModelLayer>,
        action: () -> Unit
    ) {
        setLayerContext(buffer, state, layers)
        action()
        resetLayerContext()
    }

    fun setLayerContext(buffer: VertexConsumerProvider, state: PosableState, layers: Iterable<ModelLayer>) {
        currentLayers = layers
        bufferProvider = buffer
        currentState = state
    }

    fun resetLayerContext() {
        currentLayers = emptyList()
        bufferProvider = null
        currentState = null
    }

    /**
     * Registers a pose for this model.
     *
     * @param poseType The type of pose it is, as a [PoseType]
     * @param condition The condition for this pose to apply
     * @param animations The pose animations to use as idles unless a [ActiveAnimation] prevents it.
     * @param transformedParts All the transformed forms of parts of the body that define this pose.
     */
    fun registerPose(
        poseType: PoseType,
        condition: ((PosableState) -> Boolean)? = null,
        transformTicks: Int = 10,
        namedAnimations: MutableMap<String, ExpressionLike> = mutableMapOf(),
        onTransitionedInto: (PosableState) -> Unit = {},
        animations: Array<PoseAnimation> = emptyArray(),
        transformedParts: Array<ModelPartTransformation> = emptyArray(),
        quirks: Array<ModelQuirk<*>> = emptyArray()
    ): Pose {
        return Pose(
            poseType.name,
            setOf(poseType),
            condition,
            onTransitionedInto,
            transformTicks,
            namedAnimations,
            animations,
            transformedParts,
            quirks
        ).also {
            poses[poseType.name] = it
        }
    }

    fun registerPose(
        poseName: String,
        poseTypes: Set<PoseType>,
        condition: ((PosableState) -> Boolean)? = null,
        transformTicks: Int = 10,
        namedAnimations: MutableMap<String, ExpressionLike> = mutableMapOf(),
        onTransitionedInto: (PosableState) -> Unit = {},
        animations: Array<PoseAnimation> = emptyArray(),
        transformedParts: Array<ModelPartTransformation> = emptyArray(),
        quirks: Array<ModelQuirk<*>> = emptyArray()
    ): Pose {
        return Pose(
            poseName,
            poseTypes,
            condition,
            onTransitionedInto,
            transformTicks,
            namedAnimations,
            animations,
            transformedParts,
            quirks
        ).also {
            poses[poseName] = it
        }
    }

    fun registerPose(
        poseName: String,
        poseType: PoseType,
        condition: ((PosableState) -> Boolean)? = null,
        transformTicks: Int = 10,
        namedAnimations: MutableMap<String, ExpressionLike> = mutableMapOf(),
        onTransitionedInto: (PosableState) -> Unit = {},
        animations: Array<PoseAnimation> = emptyArray(),
        transformedParts: Array<ModelPartTransformation> = emptyArray(),
        quirks: Array<ModelQuirk<*>> = emptyArray()
    ): Pose {
        return Pose(
            poseName,
            setOf(poseType),
            condition,
            onTransitionedInto,
            transformTicks,
            namedAnimations,
            animations,
            transformedParts,
            quirks
        ).also {
            poses[poseName] = it
        }
    }

    /** Registers the same configuration for both left and right shoulder poses. */
    fun registerShoulderPoses(
        transformTicks: Int = 30,
        animations: Array<PoseAnimation>,
        transformedParts: Array<ModelPartTransformation> = emptyArray()
    ) {
        registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            transformTicks = transformTicks,
            animations = animations,
            transformedParts = transformedParts
        )

        registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            transformTicks = transformTicks,
            animations = animations,
            transformedParts = transformedParts
        )
    }

    fun ModelPart.registerChildWithAllChildren(name: String): ModelPart {
        val child = this.getChild(name)!!
        registerRelevantPart(name to child)
        loadAllNamedChildren(child)
        return child
    }

    /** Builds the [locatorAccess] based on the given root part. */
    fun initializeLocatorAccess() {
        locatorAccess = LocatorAccess.resolve(rootPart) ?: LocatorAccess(rootPart)
    }

    fun getPart(name: String) = relevantPartsByName[name]!!

    fun loadAllNamedChildren(bone: Bone) {
        if (bone is ModelPart) loadAllNamedChildren(bone)
    }

    fun loadAllNamedChildren(modelPart: ModelPart) {
        for ((name, child) in modelPart.children.entries) {
            val default = ModelPartTransformation.derive(child)
            relevantPartsByName[name] = child
            defaultPositions.add(default)
            loadAllNamedChildren(child)
        }
    }

    fun registerRelevantPart(name: String, part: ModelPart): ModelPart {
        val default = ModelPartTransformation.derive(part)
        relevantPartsByName[name] = part
        defaultPositions.add(default)
        return part
    }

    fun registerRelevantPart(pairing: Pair<String, ModelPart>) = registerRelevantPart(pairing.first, pairing.second)

    /** Renders the model. Assumes rotations have been set. Will simply render the base model and then any extra layers. */
    fun render(
        context: RenderContext,
        stack: MatrixStack,
        buffer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        val r = (color shr 16 and 255) / 255F
        val g = (color shr 8 and 255) / 255F
        val b = (color and 255) / 255F
        val a = (color shr 24 and 255) / 255F

        val r2 = r * red
        val g2 = g * green
        val b2 = b * blue
        val a2 = a * alpha

        val color2 = (a2 * 255).toInt() shl 24 or ((r2 * 255).toInt() shl 16) or ((g2 * 255).toInt() shl 8) or (b2 * 255).toInt()

        rootPart.render(
            context,
            stack,
            buffer,
            packedLight,
            packedOverlay,
            color2
        )

        val provider = bufferProvider
        if (provider != null) {
            for (layer in currentLayers) {
                val texture = layer.texture?.invoke(currentState?.animationSeconds ?: 0F) ?: continue
                val renderLayer = getLayer(texture, layer.emissive, layer.translucent)
                val consumer = provider.getBuffer(renderLayer)
                val tint = layer.tint
                val tintRed = (tint.x * 255).toInt()
                val tintGreen = (tint.y * 255).toInt()
                val tintBlue = (tint.z * 255).toInt()
                val tintAlpha = (tint.w * 255).toInt()
                val tintColor = tintAlpha shl 24 or (tintRed shl 16) or (tintGreen shl 8) or tintBlue

                stack.push()
                rootPart.render(
                    context,
                    stack,
                    consumer,
                    packedLight,
                    packedOverlay,
                    tintColor
                )
                stack.pop()
            }
        }
    }

    /** Generates a [RenderLayer] by the power of god and anime. Only possible thanks to 100 access wideners. */
    fun makeLayer(texture: Identifier, emissive: Boolean, translucent: Boolean): RenderLayer {
        val multiPhaseParameters: RenderLayer.MultiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
            .program(
                when {
                    emissive && translucent -> RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM
                    !emissive && translucent -> RenderPhase.ENTITY_TRANSLUCENT_PROGRAM
                    !emissive && !translucent -> RenderPhase.ENTITY_CUTOUT_PROGRAM
                    else -> RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM // This one should be changed to maybe a custom shader? Translucent stuffs with things
                }
            )
            .texture(RenderPhase.Texture(texture, false, false))
            .transparency(if (translucent) RenderPhase.TRANSLUCENT_TRANSPARENCY else RenderPhase.NO_TRANSPARENCY)
            .cull(RenderPhase.ENABLE_CULLING)
            .writeMaskState(RenderPhase.ALL_MASK)
            .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
            .build(false)

        return RenderLayer.of(
            "cobblemon_entity_layer",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            256,
            true,
            translucent,
            multiPhaseParameters
        )
    }

    /** Makes a [RenderLayer] in a jank way. Mostly works so that's cool. */
    fun getLayer(texture: Identifier, emissive: Boolean, translucent: Boolean): RenderLayer {
        return if (!emissive && !translucent) {
            RenderLayer.getEntityCutout(texture)
        } else if (!emissive) {
            RenderLayer.getEntityTranslucent(texture)
        } else {
            makeLayer(texture, emissive = emissive, translucent = translucent)
        }
    }


    /** Applies the given pose's [ModelPartTransformation]s to the model, if there is a matching pose. */
    fun applyPose(state: PosableState, pose: Pose, intensity: Float) = pose.transformedParts.forEach { it.apply(state, intensity) }
    /** Gets the first pose of this model that matches the given [PoseType]. */
    fun getPose(pose: PoseType) = poses.values.firstOrNull { pose in it.poseTypes }
    fun getPose(name: String) = poses[name]

    /** Puts the model back to its original location and rotations. */
    fun setDefault() = defaultPositions.forEach { it.set() }

    /**
     * Finds the first of the model's poses that the given state and optional [PoseType] is appropriate for.
     * If none exists, return the first pose. Only possible with bad configuration, though.
     */
    fun getFirstSuitablePose(state: PosableState, poseType: PoseType?): Pose {
        return poses.values.firstOrNull { (poseType == null || poseType in it.poseTypes) && it.isSuitable(state) } ?: poses.values.first()
    }

    /**
     * Validates that the current pose is valid for the state and model. If it isn't, it will attempt
     * to find the most desirable pose and begin transitioning to it.
     *
     * @return the current pose that should be applied during rendering.
     */
    fun validatePose(entity: PosableEntity?, state: PosableState): Pose {
        val poseName = state.currentPose
        val currentPose = poseName?.let(poses::get)
        val entityPoseType = if (entity is PosableEntity) entity.getCurrentPoseType() else null

        // Is there any reason why we should actually change the pose?
        if (entity != null && (poseName == null || currentPose == null || !currentPose.isSuitable(state) || entityPoseType !in currentPose.poseTypes)) {
            val desirablePose = getFirstSuitablePose(state, entityPoseType)
            // If this if succeeds then it just no longer fits this pose
            if (currentPose != null) {
                // Don't apply pose correction until the current primary animation is complete.
                if (state.primaryAnimation == null) {
                   moveToPose(state, desirablePose)
                    return desirablePose
                }
            } else {
                state.setPose(desirablePose.poseName)
                return desirablePose
            }
        } else if (currentPose == null) {
            return poses.values.firstOrNull() ?: run {
                throw IllegalStateException("Model has no poses: ${this::class.simpleName}")
            }
        }

        return currentPose
    }

    /**
     * Applies animations to the current model. This is the main entry point for rendering the model. There is a lot
     * of logic here.
     */
    fun applyAnimations(
        entity: Entity?,
        state: PosableState,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        context.put(RenderContext.ENTITY, entity)
        setupEntityTypeContext(entity)
        state.currentModel = this
        // Resets the model's joints back to their default positions.
        setDefault()
        // Applies any of the state's queued actions.
        state.preRender()
        // Performs a check that the current pose is correct and returns back which pose we should be applying. Even if
        // a change of pose is necessary, if it's going to gradually transition there then we're still going to keep
        // applying our current pose until that process is done.
        val pose = validatePose(entity as? PosableEntity, state)
        // Applies the pose's transformations to the model. This is not the animations.
        applyPose(state, pose, 1F)

        val primaryAnimation = state.primaryAnimation

        // Quirks will run if there is no primary animation running and quirks are enabled for this context.
        if (primaryAnimation == null && context.request(RenderContext.DO_QUIRKS) != false) {
            // Remove any quirk animations that don't exist in our current pose
            state.quirks.keys.filterNot(pose.quirks::contains).forEach(state.quirks::remove)
            // Tick all the quirks
            pose.quirks.forEach {
                it.apply(context, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 1F)
            }
        }

        if (primaryAnimation != null) {
            // The pose intensity is the complement of the primary animation's curve. This is used to blend the primary.
            state.poseIntensity = 1 - primaryAnimation.curve((state.animationSeconds - primaryAnimation.started) / primaryAnimation.duration)
            // If the primary animation is done after running we're going to clean things up.
            if (!primaryAnimation.run(
                    context,
                    this,
                    state,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    headYaw,
                    headPitch,
                    1 - state.poseIntensity
                )
            ) {
                primaryAnimation.afterAction.accept(Unit)
                state.primaryAnimation = null
                state.poseIntensity = 1F
            }
        }

        // Run active animations and return back any that are done and can be removed.
        val removedActiveAnimations = state.activeAnimations.toList()
            .filterNot { it.run(context, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 1F) }
        state.activeAnimations.removeAll(removedActiveAnimations)
        // Applies the pose's animations.
        state.currentPose?.let(poses::get)
            ?.apply(context, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
        // Updates the locator positions now that all the animations are in effect. This is the last thing we do!
        updateLocators(entity, state)
    }

    //This is used to set additional entity type specific context
    open fun setupEntityTypeContext(entity: Entity?) {}

    /**
     * Attempts to move the given [state] to the [desirablePose], using transitions if possible. The logic for this
     * can be a bit confusing. Returns what the current pose is, just in case it has instantly changed.
     */
    fun moveToPose(state: PosableState, desirablePose: Pose): String {
        // If we're currently in a pose that doesn't exist on this model, then shit, don't even try doing anything fancy.
        val previousPose = state.currentPose?.let(poses::get) ?: run {
            state.setPose(desirablePose.poseName)
            return desirablePose.poseName
        }

        val desirablePoseType = desirablePose.poseTypes.first()

        // If we're already running a transition animation then leave it be.
        if (state.activeAnimations.none { it.isTransition }) {
            // Check for a dedicated transition animation.
            val transition = previousPose.transitions[desirablePose.poseName]
            val animation = if (transition == null && previousPose.transformTicks > 0) {
                // If no dedicated transition exists then use a simple interpolator.
                PrimaryAnimation(
                    PoseTransitionAnimation(
                        beforePose = previousPose,
                        afterPose = desirablePose,
                        durationTicks = previousPose.transformTicks
                    ),
                    curve = { 1F }
                )
            } else if (transition != null) {
                // If we have a dedicated transition, run with that. If it isn't already a PrimaryAnimation then make it one.
                var transitionAnimation = transition(previousPose, desirablePose)
                if (transitionAnimation !is PrimaryAnimation) {
                    transitionAnimation = PrimaryAnimation(transitionAnimation, curve = { 1F })
                }
                transitionAnimation.isTransition = true
                transitionAnimation
            } else {
                state.setPose(poses.values.first {
                    desirablePoseType in it.poseTypes && (it.condition == null || it.condition.invoke(state))
                }.poseName)
                return previousPose.poseName
            }

            // Set the primary animation to the transition. After the animation completes, directly set the pose since
            // we're done. The afterAction can occur from render or from tick while off-screen, either will do.
            state.addPrimaryAnimation(animation)
            animation.afterAction += {
                state.setPose(desirablePose.poseName)
                if (state.primaryAnimation == animation) {
                    state.primaryAnimation = null
                }
            }
        }
        return previousPose.poseName
    }

    /**
     * Figures out where all of this model's locators are in real space, so that they can be
     * found and used from other client-side systems.
     */
    fun updateLocators(entity: Entity?, state: PosableState) {
        entity ?: return
        val matrixStack = MatrixStack()
        var scale = 1F
        // We could improve this to be generalized for other entities. First we'd have to figure out wtf is going on, though.
        if (entity is PokemonEntity) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - entity.bodyYaw))
            matrixStack.push()
            matrixStack.scale(-1F, -1F, 1F)
            scale = entity.pokemon.form.baseScale * entity.pokemon.scaleModifier * (entity.delegate as PokemonClientDelegate).entityScaleModifier
            matrixStack.scale(scale, scale, scale)
        } else if (entity is EmptyPokeBallEntity) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.yaw))
            matrixStack.push()
            matrixStack.scale(1F, -1F, -1F)
            scale = 0.7F
            matrixStack.scale(scale, scale, scale)
        } else if (entity is GenericBedrockEntity) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.yaw))
            matrixStack.push()
            // Not 100% convinced we need the -1 on Y but if we needed it for the Poke Ball then probably?
            matrixStack.scale(1F, -1F, 1F)
        } else if (entity is NPCEntity) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - entity.bodyYaw))
            matrixStack.push()
            matrixStack.scale(-1F, -1F, 1F)
        }

        if (isForLivingEntityRenderer) {
            // Standard living entity offset, only God knows why Mojang did this.
            matrixStack.translate(0.0, -1.5, 0.0)
        }

        locatorAccess.update(matrixStack, entity, scale, state.locatorStates, isRoot = true)
    }

    fun ModelPart.translation(
        function: WaveFunction,
        axis: Int,
        timeVariable: (state: PosableState, limbSwing: Float, ageInTicks: Float) -> Float?
    ) = TranslationFunctionPoseAnimation(
        part = this,
        function = function,
        axis = axis,
        timeVariable = timeVariable
    )

    fun ModelPart.rotation(
        function: WaveFunction,
        axis: Int,
        timeVariable: (state: PosableState, limbSwing: Float, ageInTicks: Float) -> Float?
    ) = RotationFunctionPoseAnimation(
        part = this,
        function = function,
        axis = axis,
        timeVariable = timeVariable
    )

    fun bedrock(
        animationGroup: String,
        animation: String,
        animationPrefix: String = "animation.$animationGroup"
    ) = BedrockPoseAnimation(
        BedrockAnimationRepository.getAnimation(animationGroup, "$animationPrefix.$animation")
    )

    fun bedrockStateful(
        animationGroup: String,
        animation: String,
        animationPrefix: String = "animation.$animationGroup"
    ) = BedrockActiveAnimation(BedrockAnimationRepository.getAnimation(animationGroup, "$animationPrefix.$animation"))

    fun quirk(
        secondsBetweenOccurrences: Pair<Float, Float> = 8F to 30F,
        loopTimes: IntRange = 1..1,
        condition: (state: PosableState) -> Boolean = { true },
        animation: (state: PosableState) -> ActiveAnimation
    ) = SimpleQuirk(
        secondsBetweenOccurrences = secondsBetweenOccurrences,
        loopTimes = loopTimes,
        condition = condition,
        animations = { listOf(animation(it)) }
    )

    fun quirkMultiple(
        secondsBetweenOccurrences: Pair<Float, Float> = 8F to 30F,
        loopTimes: IntRange = 1..1,
        condition: (state: PosableState) -> Boolean = { true },
        animations: (state: PosableState) -> List<ActiveAnimation>
    ) = SimpleQuirk(
        secondsBetweenOccurrences = secondsBetweenOccurrences,
        loopTimes = loopTimes,
        condition = condition,
        animations = { animations(it) }
    )
}