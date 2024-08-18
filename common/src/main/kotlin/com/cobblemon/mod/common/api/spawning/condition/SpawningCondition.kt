/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.MoonPhaseRange
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.util.Merger
import com.cobblemon.mod.common.util.math.orMax
import com.cobblemon.mod.common.util.math.orMin
import com.mojang.datafixers.util.Either
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.structure.Structure

/**
 * The root of spawning conditions that can be applied to a spawning context. What type
 * of spawning context it can be applied to is relevant for any subclasses.
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
abstract class SpawningCondition<T : SpawningContext> {
    companion object {
        val conditionTypes = mutableMapOf<String, Class<out SpawningCondition<*>>>()
        fun getByName(name: String) = conditionTypes[name]
        fun <T : SpawningContext, C : SpawningCondition<T>> register(name: String, clazz: Class<C>) {
            conditionTypes[name] = clazz
        }
    }

    var dimensions: MutableList<Identifier>? = null
    var biomes: MutableSet<RegistryLikeCondition<Biome>>? = null
    var moonPhase: MoonPhaseRange? = null
    var canSeeSky: Boolean? = null
    var minX: Float? = null
    var minY: Float? = null
    var minZ: Float? = null
    var maxX: Float? = null
    var maxY: Float? = null
    var maxZ: Float? = null
    var minLight: Int? = null
    var maxLight: Int? = null
    var minSkyLight: Int? = null
    var maxSkyLight: Int? = null
    var isRaining: Boolean? = null
    var isThundering: Boolean? = null
    var timeRange: TimeRange? = null
    var structures: MutableList<Either<Identifier, TagKey<Structure>>>? = null

    @Transient
    var appendages = mutableListOf<AppendageCondition>()

    abstract fun contextClass(): Class<out T>
    fun contextMatches(ctx: SpawningContext) = contextClass().isAssignableFrom(ctx::class.java)

    fun isSatisfiedBy(ctx: SpawningContext): Boolean {
        return if (contextMatches(ctx)) {
            fits(ctx as T)
        } else {
            false
        }
    }

    protected open fun fits(ctx: T): Boolean {
        if (ctx.position.x < minX.orMin() || ctx.position.x > maxX.orMax()) {
            return false
        } else if (ctx.position.y < minY.orMin() || ctx.position.y > maxY.orMax()) {
            return false
        } else if (ctx.position.z < minZ.orMin() || ctx.position.z > maxZ.orMax()) {
            return false
        } else if (dimensions != null && dimensions!!.isNotEmpty() && ctx.world.dimensionKey.value !in dimensions!!) {
            return false
        } else if (moonPhase != null && ctx.moonPhase !in moonPhase!!) {
            return false
        } else if (biomes != null && biomes!!.isNotEmpty() && biomes!!.none { condition -> condition.fits(ctx.biome, ctx.biomeRegistry) }) {
            return false
        } else if (ctx.light > maxLight.orMax() || ctx.light < minLight.orMin()) {
            return false
        } else if (ctx.skyLight > maxSkyLight.orMax() || ctx.skyLight < minSkyLight.orMin()) {
            return false
        } else if (timeRange != null && !timeRange!!.contains((ctx.world.timeOfDay % 24000).toInt())) {
            return false
        } else if (canSeeSky != null && canSeeSky != ctx.canSeeSky) {
            return false
        } else if (isRaining != null && ctx.world.isRaining != isRaining!!) {
            return false
        } else if (isThundering != null && ctx.world.isThundering != isThundering!!) {
            return false
        } else if (appendages.any { !it.fits(ctx) }) {
            return false
        } else if (structures != null && structures!!.isNotEmpty() &&
            structures!!.let { structures ->
                val structureAccess = ctx.world.structureAccessor
                val cache = ctx.getStructureCache(ctx.position)
                return@let structures.none {
                    it.map({ cache.check(structureAccess, ctx.position, it) }, { cache.check(structureAccess, ctx.position, it) })
                }
            }
        ) {
            return false
        }

        return true
    }

    open fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        dimensions = merger.merge(dimensions, other.dimensions)?.toMutableList()
        biomes = merger.merge(biomes, other.biomes)?.toMutableSet()
        moonPhase = merger.mergeSingle(moonPhase, other.moonPhase)
        canSeeSky = merger.mergeSingle(canSeeSky, other.canSeeSky)
        minX = merger.mergeSingle(minX, other.minX)
        minY = merger.mergeSingle(minY, other.minY)
        minZ = merger.mergeSingle(minZ, other.minZ)
        maxX = merger.mergeSingle(maxX, other.maxX)
        maxY = merger.mergeSingle(maxY, other.maxY)
        maxZ = merger.mergeSingle(maxZ, other.maxZ)
        minLight = merger.mergeSingle(minLight, other.minLight)
        maxLight = merger.mergeSingle(maxLight, other.maxLight)
        minSkyLight = merger.mergeSingle(minSkyLight, other.minSkyLight)
        maxSkyLight = merger.mergeSingle(maxSkyLight, other.maxSkyLight)
        timeRange = merger.mergeSingle(timeRange, other.timeRange)
        structures = merger.merge(structures, other.structures)?.toMutableList()
    }

    open fun isValid(): Boolean {
        if(biomes != null && biomes!!.any {it == null})
            return false
        if(dimensions != null && dimensions!!.any {it == null})
            return false
        if(structures != null && structures!!.any {it == null})
            return false
        return true
    }
}