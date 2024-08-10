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
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.ModelLayer
import com.cobblemon.mod.common.client.render.layer.CobblemonRenderLayers
import com.cobblemon.mod.common.client.render.models.blockbench.animation.*
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockStatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockStatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.SimpleQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.getDoubleOrNull
import com.cobblemon.mod.common.util.getStringOrNull
import com.cobblemon.mod.common.util.plus
import com.cobblemon.mod.common.util.resolveBoolean
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

/**
 * A model that can be posed and animated using [StatelessAnimation]s and [StatefulAnimation]s. This
 * requires poses to be registered and should implement any [ModelFrame] interfaces that apply to this
 * model. Implementing the render functions is possible but not necessary.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
abstract class PoseableEntityModel<T : Entity>(
    renderTypeFunc: (Identifier) -> RenderLayer = RenderLayer::getEntityCutout
) : EntityModel<T>(renderTypeFunc), ModelFrame {
    val context: RenderContext = RenderContext()

    /** Whether the renderer that will process this is going to do the weird -1.5 Y offset bullshit that the living entity renderer does. */
    abstract val isForLivingEntityRenderer: Boolean

    val poses = mutableMapOf<String, Pose<T, out ModelFrame>>()
    lateinit var locatorAccess: LocatorAccess

    var red = 1F
    var green = 1F
    var blue = 1F
    var alpha = 1F

    @Transient
    var currentLayers: Iterable<ModelLayer> = listOf()

    @Transient
    var bufferProvider: VertexConsumerProvider? = null

    @Transient
    var currentState: PoseableEntityState<T>? = null

    val animations = mutableMapOf<String, ExpressionLike>()
    val transitions = mutableMapOf<String, ExpressionLike>()

    /**
     * A list of [ModelPartTransformation] that record the original
     * This allows the original rotations to be reset.
     */
    val defaultPositions = mutableListOf<ModelPartTransformation>()

    val relevantParts = mutableListOf<ModelPart>()
    val relevantPartsByName = mutableMapOf<String, ModelPart>()

    @Transient
    val functions = QueryStruct(hashMapOf())
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
                    curve = param.obj as WaveFunction
                    continue
                }

                val label = params.getString(index) ?: continue
                excludedLabels.add(label)
            }

            return@addFunction ObjectValue(PrimaryAnimation(animation = anim, excludedLabels = excludedLabels, curve = curve))
        }
        .addFunction("bedrock_stateful") { params ->
            val group = params.getString(0)
            val animation = params.getString(1)
            val enduresPrimary = "endures_primary_animations" in params.params.mapNotNull { it.asString() }
            val anim = bedrockStateful(group, animation)
            anim.enduresPrimaryAnimations = enduresPrimary
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
            val minYaw = params.getDoubleOrNull(6) ?: -45F
            ObjectValue(
                SingleBoneLookAnimation<T>(
                    frame = this,
                    bone = getPart(boneName),
                    pitchMultiplier = pitchMultiplier.toFloat(),
                    yawMultiplier = yawMultiplier.toFloat(),
                    maxPitch = maxPitch.toFloat(),
                    minPitch = minPitch.toFloat(),
                    maxYaw = maxYaw.toFloat(),
                    minYaw = minYaw.toFloat()
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
                QuadrupedWalkAnimation<T>(
                    frame = this,
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
                BipedWalkAnimation<T>(
                    frame = this,
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
                BimanualSwingAnimation<T>(
                    frame = this,
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
                WingFlapIdleAnimation<T>(
                    frame = this,
                    rotation = sineFunction(verticalShift = verticalShift.toFloat(), period = period.toFloat(), amplitude = amplitude.toFloat()),
                    axis = axisIndex,
                    leftWing = this.getPart(wingLeft),
                    rightWing = this.getPart(wingRight)
                )
            )
        }
        .addFunction("bedrock_quirk") { params ->
            val animationGroup = params.getString(0)
            val animationNames = params.get<MoValue>(1)?.let { if (it is ArrayStruct) it.map.values.map { it.asString() } else listOf(it.asString()) } ?: listOf()
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
    val runtime = MoLangRuntime().setup().setupClient().also { it.environment.getQueryStruct().addFunctions(functions.functions) }

    /** Registers the different poses this model is capable of ahead of time. Should use [registerPose] religiously. */
    abstract fun registerPoses()

    /** Gets the [PoseableEntityState] for an entity. */
    abstract fun getState(entity: T): PoseableEntityState<T>

    fun getAnimation(state: PoseableEntityState<*>, name: String, runtime: MoLangRuntime): StatefulAnimation<T, *>? {
        val poseAnimations = state.currentPose?.let(this::getPose)?.animations ?: mapOf()
        val animation = resolveFromAnimationMap(poseAnimations, name, runtime)
            ?: resolveFromAnimationMap(animations, name, runtime)
            ?: if (name == "cry" && this is PokemonPoseableModel) {
                cryAnimation.invoke(state.getEntity() as PokemonEntity, state as PoseableEntityState<PokemonEntity>) as? StatefulAnimation<T, *>
            } else if (name == "faint" && this is PokemonPoseableModel) {
                getFaintAnimation(state.getEntity() as PokemonEntity, state as PoseableEntityState<PokemonEntity>) as? StatefulAnimation<T, *>
            } else {
                try {
                    name.asExpressionLike().resolveObject(runtime).obj as StatefulAnimation<T, *>
                } catch (exception: Exception) {
                    extractAnimation(name)
                }
            }
        return animation
    }

    /**
     * Animation group : animation name [: primary]
     * e.g. "particle_dummy:animation.particle_dummy.dragon_claw_target:primary"
     * e.g. "particle_dummy:animation.particle.dummy.stat_up
     */
    fun extractAnimation(string: String): StatefulAnimation<T, *>? {
        val group = string.substringBefore(":")
        val animationName = string.substringAfter(":").substringBefore(":")
        val isPrimary = string.endsWith(":primary")
        if (animationName.isNotBlank() && animationName != string) {
            val animation = BedrockAnimationRepository.tryGetAnimation(group, animationName) ?: return null
            return if (isPrimary) {
                PrimaryAnimation(BedrockStatefulAnimation(animation))
            } else {
                BedrockStatefulAnimation(animation)
            }
        } else {
            return null
        }
    }

    private fun resolveFromAnimationMap(map: Map<String, ExpressionLike>, name: String, runtime: MoLangRuntime): StatefulAnimation<T, *>? {
        val animationExpression = map[name] ?: return null
        return try {
            animationExpression.resolveObject(runtime).obj as StatefulAnimation<T, *>
        } catch (e: Exception) {
            LOGGER.error("Failed to create animation by name $name, most likely something wrong in the MoLang")
            e.printStackTrace()
            null
        }
    }

    fun withLayerContext(
        buffer: VertexConsumerProvider,
        state: PoseableEntityState<T>?,
        layers: Iterable<ModelLayer>,
        action: () -> Unit
    ) {
        setLayerContext(buffer, state, layers)
        action()
        resetLayerContext()
    }

    fun setLayerContext(buffer: VertexConsumerProvider, state: PoseableEntityState<T>?, layers: Iterable<ModelLayer>) {
        currentLayers = layers
        bufferProvider = buffer
        currentState = state
    }

    fun resetLayerContext() {
        currentLayers = emptyList()
        bufferProvider = null
        currentState = null
    }

    open fun getOverlayTexture(entity: Entity?): Int? {
        return if (entity is LivingEntity) {
            OverlayTexture.packUv(
                OverlayTexture.getU(0F),
                OverlayTexture.getV(entity.hurtTime > 0 || entity.deathTime > 0)
            )
        } else if (entity != null) {
            OverlayTexture.DEFAULT_UV
        } else {
            null
        }
    }

    /**
     * Registers a pose for this model.
     *
     * @param poseType The type of pose it is, as a [PoseType]
     * @param condition The condition for this pose to apply
     * @param idleAnimations The stateless animations to use as idles unless a [StatefulAnimation] prevents it.
     * @param transformedParts All the transformed forms of parts of the body that define this pose.
     */
    fun <F : ModelFrame> registerPose(
        poseType: PoseType,
        condition: ((T) -> Boolean)? = null,
        transformTicks: Int = 10,
        animations: MutableMap<String, ExpressionLike> = mutableMapOf(),
        onTransitionedInto: (PoseableEntityState<T>?) -> Unit = {},
        idleAnimations: Array<StatelessAnimation<T, out F>> = emptyArray(),
        transformedParts: Array<ModelPartTransformation> = emptyArray(),
        quirks: Array<ModelQuirk<T, *>> = emptyArray()
    ): Pose<T, F> {
        return Pose(
            poseType.name,
            setOf(poseType),
            condition,
            onTransitionedInto,
            transformTicks,
            animations,
            idleAnimations,
            transformedParts,
            quirks
        ).also {
            setPose(poseType.name, it)
        }
    }

    fun <F : ModelFrame> registerPose(
        poseName: String,
        poseTypes: Set<PoseType>,
        condition: ((T) -> Boolean)? = null,
        transformTicks: Int = 10,
        animations: MutableMap<String, ExpressionLike> = mutableMapOf(),
        onTransitionedInto: (PoseableEntityState<T>?) -> Unit = {},
        idleAnimations: Array<StatelessAnimation<T, out F>> = emptyArray(),
        transformedParts: Array<ModelPartTransformation> = emptyArray(),
        quirks: Array<ModelQuirk<T, *>> = emptyArray()
    ): Pose<T, F> {
        return Pose(
            poseName,
            poseTypes,
            condition,
            onTransitionedInto,
            transformTicks,
            animations,
            idleAnimations,
            transformedParts,
            quirks
        ).also {
            setPose(poseName, it)
        }
    }

    fun <F : ModelFrame> registerPose(
        poseName: String,
        poseType: PoseType,
        condition: ((T) -> Boolean)? = null,
        transformTicks: Int = 10,
        animations: MutableMap<String, ExpressionLike> = mutableMapOf(),
        onTransitionedInto: (PoseableEntityState<T>?) -> Unit = {},
        idleAnimations: Array<StatelessAnimation<T, out F>> = emptyArray(),
        transformedParts: Array<ModelPartTransformation> = emptyArray(),
        quirks: Array<ModelQuirk<T, *>> = emptyArray()
    ): Pose<T, F> {
        return Pose(
            poseName,
            setOf(poseType),
            condition,
            onTransitionedInto,
            transformTicks,
            animations,
            idleAnimations,
            transformedParts,
            quirks
        ).also {
            setPose(poseName, it)
        }
    }

    fun <F : ModelFrame> setPose(poseName: String, pose: Pose<T, F>) {
        // if pose already exists for poseName, log the name of the class and pose name
        if (poses.containsKey(poseName)) {
            LOGGER.error("Pose with name $poseName already exists for class ${this::class.simpleName}")
        }
        poses[poseName] = pose
    }

    fun ModelPart.registerChildWithAllChildren(name: String): ModelPart {
        val child = this.getChild(name)!!
        registerRelevantPart(name to child)
        loadAllNamedChildren(child)
        return child
    }

    fun ModelPart.registerChildWithSpecificChildren(name: String, nameList: Iterable<String>): ModelPart {
        val child = getChild(name)
        registerRelevantPart(name to child)
        loadSpecificNamedChildren(child, nameList)
        return child
    }

    fun initializeLocatorAccess() {
        locatorAccess = LocatorAccess.resolve(rootPart) ?: LocatorAccess(rootPart)
    }

    fun getPart(name: String) = relevantPartsByName[name]!!
    fun getPartFallback(vararg names: String) = names.firstNotNullOfOrNull { relevantPartsByName[it] } ?: rootPart

    private fun loadSpecificNamedChildren(modelPart: ModelPart, nameList: Iterable<String>) {
        for ((name, child) in modelPart.children.entries) {
            if (name in nameList) {
                val default = ModelPartTransformation.derive(child)
                relevantParts.add(child)
                relevantPartsByName[name] = child
                defaultPositions.add(default)
                loadAllNamedChildren(child)
            }
        }
    }

    fun loadAllNamedChildren(bone: Bone) {
        if (bone is ModelPart) loadAllNamedChildren(bone)
    }

    fun loadAllNamedChildren(modelPart: ModelPart) {
        for ((name, child) in modelPart.children.entries) {
            val default = ModelPartTransformation.derive(child)
            relevantParts.add(child)
            relevantPartsByName[name] = child
            defaultPositions.add(default)
            loadAllNamedChildren(child)
        }
    }

    fun registerRelevantPart(name: String, part: ModelPart): ModelPart {
        val default = ModelPartTransformation.derive(part)
        relevantParts.add(part)
        relevantPartsByName[name] = part
        defaultPositions.add(default)
        return part
    }

    fun registerRelevantPart(pairing: Pair<String, ModelPart>) = registerRelevantPart(pairing.first, pairing.second)

    override fun render(
        stack: MatrixStack,
        buffer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        render(context, stack, buffer, packedLight, packedOverlay, r, g, b, a)
    }

    fun render(
        context: RenderContext,
        stack: MatrixStack,
        buffer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        rootPart.render(
            context,
            stack,
            buffer,
            packedLight,
            getOverlayTexture(context.request(RenderContext.ENTITY)) ?: packedOverlay,
            red * r,
            green * g,
            blue * b,
            alpha * a
        )

        val provider = bufferProvider
        if (provider != null) {
            for (layer in currentLayers) {
                val texture = layer.texture?.invoke(currentState?.animationSeconds ?: 0F) ?: continue
                val renderLayer = getLayer(texture, layer.emissive, layer.translucent)
                val consumer = provider.getBuffer(renderLayer)
                stack.push()
                rootPart.render(
                    context,
                    stack,
                    consumer,
                    packedLight,
                    getOverlayTexture(context.request(RenderContext.ENTITY)) ?: packedOverlay,
                    layer.tint.x,
                    layer.tint.y,
                    layer.tint.z,
                    layer.tint.w
                )
                stack.pop()
            }
        }
    }

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

    fun getLayer(texture: Identifier, emissive: Boolean, translucent: Boolean): RenderLayer {
        return if (!emissive && !translucent) {
            CobblemonRenderLayers.ENTITY_CUTOUT.apply(texture)
        } else if (!emissive) {
            CobblemonRenderLayers.ENTITY_TRANSLUCENT.apply(texture, true)
        } else {
            makeLayer(texture, emissive = emissive, translucent = translucent)
        }
    }


    /** Applies the given pose type to the model, if there is a matching pose. */
    fun applyPose(pose: String, intensity: Float) = getPose(pose)?.transformedParts?.forEach { it.apply(intensity) }
    fun getPose(pose: PoseType) = poses.values.firstOrNull { pose in it.poseTypes }
    fun getPose(name: String) = poses[name]

    /** Puts the model back to its original location and rotations. */
    fun setDefault() = defaultPositions.forEach { it.set() }

    val quirks = mutableListOf<ModelQuirk<T, *>>()

    /**
     * Sets up the angles and positions for the model knowing that there is no state. Is given a pose type to use,
     * and optionally things like limb swinging and head rotations.
     */
    fun setupAnimStateless(
        poseType: PoseType,
        limbSwing: Float = 0F,
        limbSwingAmount: Float = 0F,
        headYaw: Float = 0F,
        headPitch: Float = 0F,
        ageInTicks: Float = 0F
    ) {
        setupAnimStateless(setOf(poseType), limbSwing, limbSwingAmount, headYaw, headPitch, ageInTicks)
    }

    /**
     * Sets up the angles and positions for the model knowing that there is no state. Is given a list of pose types,
     * and it will use the first of these that is defined for the model.
     */
    fun setupAnimStateless(
        poseTypes: Set<PoseType>,
        limbSwing: Float = 0F,
        limbSwingAmount: Float = 0F,
        headYaw: Float = 0F,
        headPitch: Float = 0F,
        ageInTicks: Float = 0F
    ) {
        this.context.pop(RenderContext.ENTITY)
        setDefault()
        val pose = poseTypes.firstNotNullOfOrNull { getPose(it) } ?: poses.values.first()
        pose.transformedParts.forEach { it.apply(intensity = 1F) }
        pose.idleStateless(this, null, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 1F)
    }

    fun setupAnimStateful(
        entity: T?,
        state: PoseableEntityState<T>,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        context.put(RenderContext.ENTITY, entity)
        setupEntityTypeContext(entity)
        state.currentModel = this
        setDefault()
        if (entity != null) {
            updateLocators(state)
        }
        state.preRender()
        var poseName = state.getPose()
        var pose = poseName?.let { getPose(it) }
        val entityPoseType = if (entity is Poseable) entity.getCurrentPoseType() else null

        if (entity != null && (poseName == null || pose == null || !pose.isSuitable(entity) || entityPoseType !in pose.poseTypes)) {
            val desirablePose = poses.values.firstOrNull {
                (entityPoseType == null || entityPoseType in it.poseTypes) && it.isSuitable(entity)
            }
                ?: Pose("none", setOf(PoseType.NONE), null, {}, 0, mutableMapOf(), emptyArray(), emptyArray(), emptyArray())

            // If this condition matches then it just no longer fits this pose
            if (pose != null && poseName != null) {
                if (state.primaryAnimation == null) {
                    moveToPose(entity, state, desirablePose)
                }
            } else {
                pose = desirablePose
                poseName = desirablePose.poseName
                getState(entity).setPose(poseName)
            }
        } else {
            poseName = poseName ?: poses.values.first().poseName
        }

        val currentPose = getPose(poseName)
        applyPose(poseName, 1F)

        val primaryAnimation = state.primaryAnimation

        if (currentPose != null && primaryAnimation == null) {
            // Remove any quirk animations that don't exist in our current pose
            state.quirks.keys.filterNot(currentPose.quirks::contains).forEach(state.quirks::remove)
            // Tick all the quirks
            currentPose.quirks.forEach {
                it.tick(entity, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 1F)
            }
        }

        if (primaryAnimation != null) {
            val portion = (state.animationSeconds - primaryAnimation.started) / primaryAnimation.duration
            state.primaryOverridePortion = 1 - primaryAnimation.curve(portion)
            if (!primaryAnimation.run(entity, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 1 - state.primaryOverridePortion)) {
                primaryAnimation.afterAction.accept(Unit)
                state.primaryAnimation = null
                state.primaryOverridePortion = 1F
            }
        }

        val removedStatefuls = state.statefulAnimations.toList()
            .filterNot { it.run(entity, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 1F) }
        state.statefulAnimations.removeAll(removedStatefuls)
        state.currentPose?.let { getPose(it) }
            ?.idleStateful(entity, this, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
        if (entity != null) {
            updateLocators(state)
        }
    }

    //This is used to set additional entity type specific context
    open fun setupEntityTypeContext(entity: T?) {}

    //Called by LivingEntityRenderer's render method before calling model.render (which is this.render in this case)
    override fun setAngles(
        entity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        setupAnimStateful(entity, getState(entity), limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
    }

    fun moveToPose(entity: T?, state: PoseableEntityState<T>, desirablePose: Pose<T, out ModelFrame>) {
        val previousPose = state.getPose()?.let { getPose(it) } ?: run {
            return state.setPose(desirablePose.poseName)
        }

        val desirablePoseType = desirablePose.poseTypes.first()

        if (state.statefulAnimations.none { it.isTransform }) {
            val transition = previousPose.transitions[desirablePose.poseName]
            if (transition == null && previousPose.transformTicks > 0) {
                val primaryAnimation = PrimaryAnimation(
                    PoseTransitionAnimation(
                        beforePose = previousPose,
                        afterPose = desirablePose,
                        durationTicks = previousPose.transformTicks
                    ),
                    curve = { 1F }
                )
                state.addPrimaryAnimation(primaryAnimation)
                primaryAnimation.afterAction += {
                    state.setPose(desirablePose.poseName)
                }
            } else if (transition != null) {
                val animation = transition(previousPose, desirablePose)
                val primaryAnimation = if (animation is PrimaryAnimation) animation else PrimaryAnimation(animation, curve = { 1F })
                primaryAnimation.afterAction += {
                    state.setPose(desirablePose.poseName)
                }
                state.addPrimaryAnimation(primaryAnimation)
            } else {
                state.setPose(poses.values.first { desirablePoseType in it.poseTypes && (it.condition == null || (entity != null && it.condition.invoke(entity))) }.poseName)
            }
        }
    }

    /**
     * Figures out where all of this model's locators are in real space, so that they can be
     * found and used from other client-side systems.
     */
    fun updateLocators(state: PoseableEntityState<T>) {
        val entity = context.request(RenderContext.ENTITY) ?: return
        val matrixStack = MatrixStack()
        var scale = 1F
        // We could improve this to be generalized for other entities
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
        }

        val states = state.locatorStates

        if (isForLivingEntityRenderer) {
            // Standard living entity offset, only God knows why Mojang did this.
            matrixStack.translate(0.0, -1.5, 0.0)
        }

        locatorAccess.update(matrixStack, entity, scale, states, isRoot = true)
    }

    fun ModelPart.translation(
        function: WaveFunction,
        axis: Int,
        timeVariable: (state: PoseableEntityState<T>?, limbSwing: Float, ageInTicks: Float) -> Float?
    ) = TranslationFunctionStatelessAnimation<T>(
        part = this,
        function = function,
        axis = axis,
        timeVariable = timeVariable,
        frame = this@PoseableEntityModel
    )

    fun ModelPart.rotation(
        function: WaveFunction,
        axis: Int,
        timeVariable: (state: PoseableEntityState<T>?, limbSwing: Float, ageInTicks: Float) -> Float?
    ) = RotationFunctionStatelessAnimation(
        part = this,
        function = function,
        axis = axis,
        timeVariable = timeVariable,
        frame = this@PoseableEntityModel
    )

    fun bedrock(
        animationGroup: String,
        animation: String,
        animationPrefix: String = "animation.$animationGroup"
    ) = BedrockStatelessAnimation<T>(
        this,
        BedrockAnimationRepository.getAnimation(animationGroup, "$animationPrefix.$animation")
    )

    fun bedrockStateful(
        animationGroup: String,
        animation: String,
        animationPrefix: String = "animation.$animationGroup"
    ) = BedrockStatefulAnimation<T>(
        BedrockAnimationRepository.getAnimation(animationGroup, "$animationPrefix.$animation"),
    )

    fun quirk(
        secondsBetweenOccurrences: Pair<Float, Float> = 8F to 30F,
        loopTimes: IntRange = 1..1,
        condition: (state: PoseableEntityState<T>) -> Boolean = { true },
        animation: (state: PoseableEntityState<T>) -> StatefulAnimation<T, *>
    ) = SimpleQuirk(
        secondsBetweenOccurrences = secondsBetweenOccurrences,
        loopTimes = loopTimes,
        condition = condition,
        animations = { listOf(animation(it)) }
    )

    fun quirkMoLangCondition(
        secondsBetweenOccurrences: Pair<Float, Float> = 8F to 30F,
        loopTimes: IntRange = 1..1,
        conditionExpression: ExpressionLike = "true".asExpressionLike(),
        animation: (state: PoseableEntityState<T>) -> StatefulAnimation<T, *>
    ) = SimpleQuirk<T>(
        secondsBetweenOccurrences = secondsBetweenOccurrences,
        loopTimes = loopTimes,
        condition = { it.runtime.resolveBoolean(conditionExpression) },
        animations = { listOf(animation(it)) }
    )

    fun quirkMultiple(
        secondsBetweenOccurrences: Pair<Float, Float> = 8F to 30F,
        loopTimes: IntRange = 1..1,
        condition: (state: PoseableEntityState<T>) -> Boolean = { true },
        animations: (state: PoseableEntityState<T>) -> List<StatefulAnimation<T, *>>
    ) = SimpleQuirk(
        secondsBetweenOccurrences = secondsBetweenOccurrences,
        loopTimes = loopTimes,
        condition = condition,
        animations = { animations(it) }
    )

    val dummyAnimation = object : StatefulAnimation<T, ModelFrame> {
        override val isTransform = false
        override val duration: Float = 1F
        override val enduresPrimaryAnimations = false

        override fun run(
            entity: T?,
            model: PoseableEntityModel<T>,
            state: PoseableEntityState<T>,
            limbSwing: Float,
            limbSwingAmount: Float,
            ageInTicks: Float,
            headYaw: Float,
            headPitch: Float,
            intensity: Float
        ): Boolean {
            return false
        }
    }
}