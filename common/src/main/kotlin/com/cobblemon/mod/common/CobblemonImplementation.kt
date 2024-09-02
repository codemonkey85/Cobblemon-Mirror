/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.tags.TagKey
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import kotlin.reflect.KClass

interface CobblemonImplementation {
    val modAPI: ModAPI

    /**
     *
     */
    val networkManager: NetworkManager

    /**
     * TODO
     *
     * @return
     */
    fun environment(): Environment

    /**
     * TODO
     *
     * @param id
     * @return
     */
    fun isModInstalled(id: String): Boolean

    /**
     * TODO
     *
     */
    fun registerPermissionValidator()

    /**
     * TODO
     *
     */
    fun registerSoundEvents()

    fun registerDataComponents()

    fun registerEntityDataSerializers()

    /**
     * TODO
     *
     */
    fun registerItems()

    /**
     * TODO
     *
     */
    fun registerBlocks()

    /**
     * TODO
     *
     */
    fun registerEntityTypes()

    /**
     * TODO
     *
     */
    fun registerEntityAttributes()

    /**
     * TODO
     *
     */
    fun registerBlockEntityTypes()

    /**
     * TODO
     *
     */
    fun registerWorldGenFeatures()

    fun registerParticles()

    fun registerEntitySubPredicates()


    /**
     * Add a feature to the current platform implementation.
     *
     * @param feature The [PlacedFeature] being added.
     * @param step The [GenerationStep.Feature] of this feature.
     * @param validTag The [TagKey] required by the [Biome] for this feature to generate in, if null all biomes are valid.
     */
    fun addFeatureToWorldGen(feature: ResourceKey<PlacedFeature>, step: GenerationStep.Decoration, validTag: TagKey<Biome>?)

    /**
     * TODO
     *
     * @param A
     * @param T
     * @param identifier
     * @param argumentClass
     * @param serializer
     */
    fun <A : ArgumentType<*>, T : ArgumentTypeInfo.Template<A>> registerCommandArgument(identifier: ResourceLocation, argumentClass: KClass<A>, serializer: ArgumentTypeInfo<A, T>)

    /**
     * TODO
     *
     * @param T
     * @param name
     * @param category
     * @param type
     * @return
     */
    fun <T : GameRules.Value<T>> registerGameRule(name: String, category: GameRules.Category, type: GameRules.Type<T>): GameRules.Key<T>

    /**
     * TODO
     *
     * @param T
     * @param criteria
     * @return
     */
    fun registerCriteria()

    /**
     * TODO
     *
     * @param identifier
     * @param reloader
     * @param type
     * @param dependencies
     */
    fun registerResourceReloader(identifier: ResourceLocation, reloader: PreparableReloadListener, type: PackType, dependencies: Collection<ResourceLocation>)

    /**
     * TODO
     *
     * @return
     */
    fun server(): MinecraftServer?

    /**
     * Handles platform independent reloading of a [JsonDataRegistry].
     *
     * @param registry The [JsonDataRegistry] to reload.
     * @param manager The [ResourceManager] to reload from.
     */
    fun <T> reloadJsonRegistry(registry: JsonDataRegistry<T>, manager: ResourceManager): HashMap<ResourceLocation, T>

    /**
     * Registers an item to the [ComposterBlock].
     *
     * @param item The [ItemLike] being registered.
     * @param chance The chance % of increasing the composter level, 0 to 1 expected.
     */
    fun registerCompostable(item: ItemLike, chance: Float)

    /**
     * Registers a builtin resource pack.
     *
     * @param id The unique [ResourceLocation] of this pack.
     * @param title The title displayed in the resource pack GUI, the description is still a part of the pack metadata.
     * @param activationBehaviour The [ResourcePackActivationBehaviour] for this pack.
     */
    fun registerBuiltinResourcePack(id: ResourceLocation, title: Component, activationBehaviour: ResourcePackActivationBehaviour)

}

enum class ResourcePackActivationBehaviour {

    /**
     * The resource pack will start disabled.
     */
    NORMAL,

    /**
     * The resource pack will start enabled.
     */
    DEFAULT_ENABLED,

    /**
     * The resource pack will always be enabled.
     * The user can reorder it but cannot remove it.
     */
    ALWAYS_ENABLED;

}

enum class ModAPI {
    FABRIC,
    FORGE,
    NEOFORGE
}

interface NetworkManager {
    fun sendPacketToPlayer(player: ServerPlayer, packet: NetworkPacket<*>)

    fun sendToServer(packet: NetworkPacket<*>)
}

enum class Environment {
    CLIENT,
    SERVER
}