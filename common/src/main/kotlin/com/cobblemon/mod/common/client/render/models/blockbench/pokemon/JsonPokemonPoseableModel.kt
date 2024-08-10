/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.JsonPose
import com.cobblemon.mod.common.client.render.models.blockbench.JsonPoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.adapters.Vec3dAdapter
import com.cobblemon.mod.common.util.isDusk
import com.cobblemon.mod.common.util.isStandingOnRedSand
import com.cobblemon.mod.common.util.isStandingOnSand
import com.cobblemon.mod.common.util.isStandingOnSandOrRedSand
import com.cobblemon.mod.common.util.resolveBoolean
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.function.Supplier
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

/**
 * The goal of this is to allow a PokemonPoseableModel to be constructed from a JSON instead of being
 * created via a class. By being loadable from JSON, the full flow of a new Pokémon can be accomplished
 * from a resource + data pack.
 *
 * @author Hiroku
 * @since August 7th, 2022
 */
class JsonPokemonPoseableModel(rootPart: Bone) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = (rootPart as ModelPart).children.entries.first().let { rootPart.registerChildWithAllChildren(it.key) }
    companion object {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Vec3d::class.java, Vec3dAdapter)
            .setExclusionStrategies(JsonPoseableEntityModel.JsonModelExclusion)
            .registerTypeAdapter(
                TypeToken.getParameterized(
                    Supplier::class.java,
                    TypeToken.getParameterized(
                        StatefulAnimation::class.java,
                        PokemonEntity::class.java,
                        ModelFrame::class.java
                    ).type
                ).type,
                JsonPoseableEntityModel.StatefulAnimationAdapter { JsonPokemonPoseableModelAdapter.model!! }
            )
            .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
            .registerTypeAdapter(Pose::class.java, PoseAdapter)
            .registerTypeAdapter(JsonPokemonPoseableModel::class.java, JsonPokemonPoseableModelAdapter)
            .create()

        fun registerFactory(id: String, factory: AnimationReferenceFactory) {
            ANIMATION_FACTORIES[id] = factory
        }

        val ANIMATION_FACTORIES = mutableMapOf<String, AnimationReferenceFactory>()
    }

    override fun registerPoses() {}

    @SerializedName("head")
    val headJoint: String? = null

    override val head: Bone by lazy { headJoint?.let { getPart(it) } ?: rootPart }

    //Some weirdness going on here, vars have to be initialized, gson doesn't like multiple fields with the same name
    //Idk, works fine with Blastoise
    override var portraitScale = 1F
    override var portraitTranslation = Vec3d.ZERO
    override var profileScale = 1F
    override var profileTranslation = Vec3d.ZERO


    val faint: Supplier<StatefulAnimation<PokemonEntity, ModelFrame>>? = null
    val cry: Supplier<StatefulAnimation<PokemonEntity, ModelFrame>>? = null

    override fun getFaintAnimation(pokemonEntity: PokemonEntity, state: PoseableEntityState<PokemonEntity>) = faint?.get()
    override val cryAnimation = CryProvider { _, _ -> cry?.get() }

    object JsonModelExclusion: ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            return f.declaringClass.simpleName !in listOf(
                "JsonPokemonPoseableModel",
                "PoseableEntityModel",
                "Pose"
            )
        }

        override fun shouldSkipClass(clazz: Class<*>): Boolean {
            return false
        }

    }

    object StatefulAnimationAdapter : JsonDeserializer<Supplier<StatefulAnimation<PokemonEntity, ModelFrame>>> {
        override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Supplier<StatefulAnimation<PokemonEntity, ModelFrame>> {
            json as JsonPrimitive
            val animString = json.asString
            val format = animString.substringBefore("(")
            return Supplier { ANIMATION_FACTORIES[format]!!.stateful(JsonPokemonPoseableModelAdapter.model!!, animString) }
        }
    }

    object JsonPokemonPoseableModelAdapter : InstanceCreator<JsonPokemonPoseableModel> {
        var modelPart: Bone? = null
        var model: JsonPokemonPoseableModel? = null
        override fun createInstance(type: Type): JsonPokemonPoseableModel {
            return JsonPokemonPoseableModel(modelPart!!).also {
                model = it
                modelPart as ModelPart
                it.loadAllNamedChildren(modelPart!!)
            }
        }
    }

    object PoseAdapter : JsonDeserializer<Pose<PokemonEntity, ModelFrame>> {
        override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Pose<PokemonEntity, ModelFrame> {
            val model = JsonPokemonPoseableModelAdapter.model!!
            val obj = json as JsonObject
            val pose = JsonPose(model, obj)

            val conditionsList = mutableListOf<(PokemonEntity) -> Boolean>()

            val mustBeInBattle = json.get("isBattle")?.asBoolean
            if (mustBeInBattle != null) {
                conditionsList.add { mustBeInBattle == it.isBattling }
            }
            val mustBeTouchingWater = json.get("isTouchingWater")?.asBoolean
            if (mustBeTouchingWater != null) {
                conditionsList.add { mustBeTouchingWater == it.isTouchingWater }
            }
            val mustBeTouchingWaterOrRain = json.get("isTouchingWaterOrRain")?.asBoolean
            if (mustBeTouchingWaterOrRain != null) {
                conditionsList.add { mustBeTouchingWaterOrRain == it.isTouchingWaterOrRain }
            }
            val mustBeSubmergedInWater = json.get("isSubmergedInWater")?.asBoolean
            if (mustBeSubmergedInWater != null) {
                conditionsList.add { mustBeSubmergedInWater == it.isSubmergedInWater }
            }
            val mustBeStandingOnRedSand = json.get("isStandingOnRedSand")?.asBoolean
            if (mustBeStandingOnRedSand != null) {
                conditionsList.add { mustBeStandingOnRedSand == it.isStandingOnRedSand() }
            }
            val mustBeStandingOnSand = json.get("isStandingOnSand")?.asBoolean
            if (mustBeStandingOnSand != null) {
                conditionsList.add { mustBeStandingOnSand == it.isStandingOnSand() }
            }
            val mustBeStandingOnSandOrRedSand = json.get("isStandingOnSandOrRedSand")?.asBoolean
            if (mustBeStandingOnSandOrRedSand != null) {
                conditionsList.add { mustBeStandingOnSandOrRedSand == it.isStandingOnSandOrRedSand() }
            }
            val mustBeDusk = json.get("isDusk")?.asBoolean
            if (mustBeDusk != null) {
                conditionsList.add { mustBeDusk == it.isDusk() }
            }

            conditionsList.add { (it.delegate as PokemonClientDelegate).runtime.resolveBoolean(pose.condition) }

            val poseCondition: ((PokemonEntity) -> Boolean)? = if (conditionsList.isEmpty()) null else conditionsList.reduce { acc, function -> { acc(it) && function(it) } }

            return Pose(
                poseName = pose.poseName,
                poseTypes = pose.poseTypes.toSet(),
                condition = poseCondition,
                animations = pose.animations,
                transformTicks = pose.transformTicks,
                idleAnimations = pose.idleAnimations,
                transformedParts = pose.transformedParts,
                quirks = pose.quirks.toTypedArray()
            ).also {
                it.transitions.putAll(
                    pose.transitions
                        .mapNotNull<JsonPose.JsonPoseTransition, Pair<String, (Pose<PokemonEntity, out ModelFrame>, Pose<PokemonEntity, out ModelFrame>) -> StatefulAnimation<PokemonEntity, ModelFrame>>> {
                            it.to to { _, _ -> it.animation.resolveObject(model.runtime).obj as StatefulAnimation<PokemonEntity, ModelFrame> }
                        }.toMap()
                )
            }
        }
    }
}