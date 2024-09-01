/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.berry.spawncondition.BerrySpawnCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.mulch.MulchVariant
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.status.Status
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.BerryRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.adapters.CobblemonStatTypeAdapter
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.awt.Color

/**
 * The data registry for [Berry].
 *
 * @author Licious
 * @since November 28th, 2022
 */
object Berries : JsonDataRegistry<Berry> {

    override val id: ResourceLocation = cobblemonResource("berries")
    override val type: PackType = PackType.SERVER_DATA
    override val observable = SimpleObservable<Berries>()

    override val gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(MulchVariant::class.java, MulchVariantAdapter)
        .registerTypeAdapter(MinMaxBounds.Doubles::class.java, FloatNumberRangeAdapter)
        .registerTypeAdapter(Status::class.java, StatusAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(Collection::class.java, AABB::class.java).type, BoxCollectionAdapter)
        .registerTypeAdapter(AABB::class.java, BoxAdapter)
        .registerTypeAdapter(Vec3::class.java, VerboseVec3dAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(GrowthFactor::class.java, CobblemonGrowthFactorAdapter)
        .registerTypeAdapter(IntRange::class.java, VerboseIntRangeAdapter)
        .registerTypeAdapter(Color::class.java, LiteralHexColorAdapter)
        .registerTypeAdapter(Stat::class.java, CobblemonStatTypeAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(TagKey::class.java, Biome::class.java).type, TagKeyAdapter(Registries.BIOME))
        .registerTypeAdapter(BerrySpawnCondition::class.java, CobblemonBerrySpawnConditionAdapter)
        .create()
    override val typeToken: TypeToken<Berry> = TypeToken.get(Berry::class.java)
    override val resourcePath = "berries"

    private val berries = hashMapOf<ResourceLocation, Berry>()

    override fun reload(data: Map<ResourceLocation, Berry>) {
        this.berries.clear()
        data.forEach { (identifier, berry) ->
            try {
                berry.identifier = identifier
                berry.validate()
                this.berries[identifier] = berry
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Skipped loading the {} berry", identifier, e)
            }
        }
        Cobblemon.LOGGER.info("Loaded {} berries", this.berries.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {
        BerryRegistrySyncPacket(this.all()).sendToPlayer(player)
    }

    fun all() = this.berries.values.toList()

    /**
     * Gets a berry if loaded.
     *
     * @param identifier The identifier of the berry.
     * @return The [Berry] if loaded otherwise null.
     */
    fun getByIdentifier(identifier: ResourceLocation): Berry? = this.berries[identifier]

    /**
     * Gets a berry if loaded.
     *
     * @param name The path of the identifier of the berry under the [Cobblemon.MODID] namespace.
     * @return The [Berry] if loaded otherwise null.
     */
    fun getByName(name: String): Berry? = this.getByIdentifier(cobblemonResource(name))

}