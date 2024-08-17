/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.StandardSpeciesFeatureSyncPacket
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.SpeciesFeatureProviderAdapter
import com.cobblemon.mod.common.util.adapters.Vec3dAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.phys.Vec3

/**
 * A registry for [SpeciesFeatureProvider]s. This is the backbone of species-specific data such as
 * variation information and counters used for some evolution types.
 *
 * Providers are loaded from JSON but can also be added programmatically. Species feature providers
 * that are registered via code will replace same-name features that were drawn from assets.
 *
 * If a species feature provider implements [CustomPokemonPropertyType] then it will be automatically
 * registered appropriately, as it will with aspects if it implements [AspectProvider].
 *
 * @author Hiroku
 * @since November 30th, 2022
 */
object SpeciesFeatures : JsonDataRegistry<SpeciesFeatureProvider<*>> {
    override val id = cobblemonResource("species_features")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<SpeciesFeatures>()

    val types = mutableMapOf<String, Class<out SpeciesFeatureProvider<*>>>()

    private val codeFeatures = mutableMapOf<String, SpeciesFeatureProvider<*>>()
    private val resourceFeatures = mutableMapOf<String, SpeciesFeatureProvider<*>>()
    override val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(SpeciesFeatureProvider::class.java, SpeciesFeatureProviderAdapter)
        .registerTypeAdapter(Vec3::class.java, Vec3dAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()
    override val typeToken: TypeToken<SpeciesFeatureProvider<*>> = TypeToken.get(SpeciesFeatureProvider::class.java)
    override val resourcePath: String = "species_features"

    override fun sync(player: ServerPlayer) {
        player.sendPacket(StandardSpeciesFeatureSyncPacket(codeFeatures + resourceFeatures))
    }

    override fun reload(data: Map<ResourceLocation, SpeciesFeatureProvider<*>>) {
        resourceFeatures.keys.toList().forEach(this::unregister)
        data.forEach(this::registerFromAssets)
    }

    fun getCodeFeature(name: String) = resourceFeatures[name]
    fun getResourceFeature(name: String) = codeFeatures[name]
    fun getFeature(name: String) = getCodeFeature(name) ?: getResourceFeature(name)
    fun loadOnClient(entries: Collection<Map.Entry<String, SpeciesFeatureProvider<*>>>) {
        codeFeatures.putAll(entries.map { it.toPair() })
    }

    fun getFeatures() = (resourceFeatures.keys + codeFeatures.keys).mapNotNull(this::getFeature)
    fun getFeaturesFor(species: Species): List<SpeciesFeatureProvider<*>> {
        val mentionedFeatures = species.features.mapNotNull(this::getFeature)
        val globalFeatures = GlobalSpeciesFeatures.getFeatures()
        val assignedFeatures = SpeciesFeatureAssignments.getFeatures(species).mapNotNull(this::getFeature)

        return (mentionedFeatures + globalFeatures + assignedFeatures).distinct()
    }

    private fun register(name: String, provider: SpeciesFeatureProvider<*>, isCoded: Boolean) {
        val mapping = if (isCoded) codeFeatures else resourceFeatures
        if (provider is AspectProvider) {
            AspectProvider.register(provider)
        }
        if (provider is CustomPokemonPropertyType<*>) {
            CustomPokemonProperty.register(provider)
        }
        mapping[name] = provider
    }

    fun register(name: String, provider: SpeciesFeatureProvider<*>) = register(name, provider, isCoded = true)
    private fun registerFromAssets(identifier: ResourceLocation, provider: SpeciesFeatureProvider<*>) = register(identifier.path, provider, isCoded = false)

    fun unregister(name: String) {
        var coded = true
        val value = getResourceFeature(name)?.also { coded = false } ?: getCodeFeature(name) ?: return
        if (value is AspectProvider) {
            AspectProvider.unregister(value)
        }
        if (value is CustomPokemonPropertyType<*>) {
            CustomPokemonProperty.unregister(value)
        }
        val mapping = if (coded) codeFeatures else resourceFeatures
        mapping.remove(name)
    }
}