/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.ModelTextureSupplierAdapter
import com.cobblemon.mod.common.util.adapters.Vector3fAdapter
import com.cobblemon.mod.common.util.adapters.Vector4fAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlin.math.floor
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * All the information required for rendering a Pokémon/Poké Ball/NPC with aspects.
 *
 * @author Hiroku
 * @since May 14th, 2022
 */
class VaryingRenderableResolver<T : PosableModel>(
    val name: ResourceLocation,
    val variations: MutableList<ModelAssetVariation>
) {
    lateinit var repository: VaryingModelRepository<T>
    val posers = mutableMapOf<Pair<ResourceLocation, ResourceLocation>, T>()
    val models = mutableMapOf<ResourceLocation, Bone>()

    fun getResolvedPoser(aspects: Set<String>): ResourceLocation {
        return getVariationValue(aspects) { poser }
            ?: throw IllegalStateException("Unable to find a poser for $name with aspects ${aspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getResolvedModel(aspects: Set<String>): ResourceLocation {
        return getVariationValue(aspects) { model }
            ?: throw IllegalStateException("Unable to find a model for $name with aspects ${aspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getResolvedTexture(aspects: Set<String>, animationSeconds: Float): ResourceLocation {
        return getVariationValue(aspects) { texture }?.invoke(animationSeconds)
            ?: throw IllegalStateException("Unable to find a texture for $name with aspects ${aspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    private fun <T> getVariationValue(aspects: Set<String>, selector: ModelAssetVariation.() -> T?): T? {
        return variations.lastOrNull { it.aspects.all { it in aspects } && selector(it) != null }?.let(selector)
    }

    fun getResolvedLayers(aspects: Set<String>): Iterable<ModelLayer> {
        val layerMaps = mutableMapOf<String, ModelLayer>()
        for (variation in variations) {
            val layers = variation.layers
            if (layers != null && variation.aspects.all { it in aspects }) {
                for (layer in layers) {
                    layerMaps[layer.name] = layer
                }
            }
        }
        return layerMaps.values.filter(ModelLayer::enabled)
    }

    fun getAllModels(): Set<ResourceLocation> {
        val models = mutableSetOf<ResourceLocation>()
        for (variation in variations) {
            if (variation.model != null) {
                models.add(variation.model)
            }
        }
        return models
    }

    companion object {
        val GSON = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
            .registerTypeAdapter(Vector3f::class.java, Vector3fAdapter)
            .registerTypeAdapter(Vector4f::class.java, Vector4fAdapter)
            .registerTypeAdapter(ModelTextureSupplier::class.java, ModelTextureSupplierAdapter)
            .disableHtmlEscaping()
            .setLenient()
            .create()
    }

    fun initialize(repository: VaryingModelRepository<T>) {
        this.repository = repository
        posers.clear()
        getAllModels().forEach { identifier ->
            try {
                models[identifier] = repository.texturedModels[identifier]!!.invoke(repository.isForLivingEntityRenderer)
            } catch (e: Exception) {
                throw IllegalStateException("Unable to load model $identifier for $name", e)
            }
        }
    }

    fun getPoser(aspects: Set<String>): T {
        val poserName = getResolvedPoser(aspects)
        val poserSupplier = repository.posers[poserName] ?: throw IllegalStateException("No poser found for name: $poserName for $name")
        val modelName = getResolvedModel(aspects)
        val existingEntityModel = posers[poserName to modelName]
        return if (existingEntityModel != null) {
            existingEntityModel
        } else {
            val model = models[modelName]!!
            val entityModel = poserSupplier(model)
            entityModel.initializeLocatorAccess()
            entityModel.registerPoses()
            posers[poserName to modelName] = entityModel
            entityModel
        }
    }

    fun getTexture(aspects: Set<String>, animationSeconds: Float): ResourceLocation {
        repository.posers[getResolvedPoser(aspects)] ?: throw IllegalStateException("No poser for $name")
        return getResolvedTexture(aspects, animationSeconds)
    }

    fun getLayers(aspects: Set<String>): Iterable<ModelLayer> {
        repository.posers[getResolvedPoser(aspects)] ?: throw IllegalStateException("No poser for $name")
        return getResolvedLayers(aspects)
    }
}

/**
 * A set of variations. This is essentially a prioritized list of [ModelAssetVariation]s, with
 * an [order] property to control the priority of this set compared to other sets.
 *
 * @author Hiroku
 * @since December 4th, 2022
 */
class ModelVariationSet(
    @SerializedName("name", alternate = ["species", "pokeball"])
    val name: ResourceLocation = cobblemonResource("thing"),
    val order: Int = 0,
    val variations: MutableList<ModelAssetVariation> = mutableListOf()
)


/**
 * A variation to the base set, which can overwrite the poser, model, texture, or any combination of the above.
 * It contains a set of aspects that must ALL be present on a renderable for this variation to be considered.
 * If a later variation also matches, but provides different properties, both this and the other variation will
 * be used for their respective non-null properties.
 *
 * @author Hiroku
 * @since May 14th, 2022
 */
class ModelAssetVariation(
    val aspects: MutableSet<String> = mutableSetOf(),
    val poser: ResourceLocation? = null,
    val model: ResourceLocation? = null,
    val texture: ModelTextureSupplier? = null,
    val layers: List<ModelLayer>? = null
)

/**
 * Given the animation seconds, returns a texture to use. Only implemented
 * by [StaticModelTextureSupplier] and [AnimatedModelTextureSupplier].
 *
 * @author Hiroku
 * @since February 6th, 2023
 */
fun interface ModelTextureSupplier {
    operator fun invoke(animationSeconds: Float): ResourceLocation
}

class StaticModelTextureSupplier(val texture: ResourceLocation): ModelTextureSupplier {
    override fun invoke(animationSeconds: Float): ResourceLocation {
        return texture
    }
}

class AnimatedModelTextureSupplier(
    val loop: Boolean,
    val fps: Float,
    val frames: List<ResourceLocation>
): ModelTextureSupplier {
    override fun invoke(animationSeconds: Float): ResourceLocation {
        val frameIndex = floor(animationSeconds * fps).toInt()
        if (frameIndex >= frames.size && !loop) {
            return frames.last()
        }
        return frames[frameIndex % frames.size]
    }
}

class ModelLayer {
    val name: String = ""
    val enabled: Boolean = true
    val tint: Vector4f = Vector4f(1F, 1F, 1F, 1F)
    val texture: ModelTextureSupplier? = null
    val emissive: Boolean = false
    val translucent: Boolean = false
}