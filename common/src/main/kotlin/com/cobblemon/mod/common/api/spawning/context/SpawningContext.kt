/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.condition.BasicSpawningCondition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.material.Fluid

/**
 * A context upon which spawning is being attempted. This supplies all the information that can be used to asynchronously
 * check for suitability of spawning.
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
abstract class SpawningContext {
    companion object {
        val contexts = mutableListOf<RegisteredSpawningContext<*>>()
        fun getByName(name: String) = contexts.find { it.name == name }
        fun getByClass(ctx: SpawningContext) = contexts.find { it.clazz == ctx::class.java }
        fun <T : SpawningContext> register(name: String, clazz: Class<T>, defaultCondition: String = BasicSpawningCondition.NAME) {
            contexts.add(
                RegisteredSpawningContext(
                    name = name,
                    clazz = clazz,
                    defaultCondition = defaultCondition
                )
            )
        }
    }

    /** What caused the spawn context, as a [SpawnCause]. */
    abstract val cause: SpawnCause
    val spawner: Spawner
        get() = cause.spawner
    /** The [ServerLevel] the spawning context exists in. */
    abstract val world: ServerLevel
    /** The location of the spawning attempt. */
    abstract val position: BlockPos
    /** The light level at this location. */
    abstract val light: Int
    /** The sky light level at this location. (15 - The distance to the nearest block that would be illuminated by the sun */
    abstract val skyLight: Int
    /** Whether or not the sky is visible at this location. */
    abstract val canSeeSky: Boolean
    /** A list of [SpawningInfluence]s that apply due to this specific context. This generally shouldn't be consumed; use [getAllInfluences].*/
    abstract val influences: MutableList<SpawningInfluence>
    /** Gets a cache of structures by block coordinates, grouped by chunk. */
    abstract fun getStructureCache(pos: BlockPos): StructureChunkCache

    /** The current phase of the moon at this location. */
    val moonPhase: Int by lazy { world.moonPhase }
    /** The biome of this location. */
    val biome: Biome by lazy { world.getBiome(position).value() }

    val biomeRegistry: Registry<Biome> by lazy { world.registryAccess().registryOrThrow(Registries.BIOME) }
    val blockRegistry: Registry<Block> by lazy { world.registryAccess().registryOrThrow(Registries.BLOCK) }
    val fluidRegistry: Registry<Fluid> by lazy { world.registryAccess().registryOrThrow(Registries.FLUID) }
    val enchantmentRegistry: Registry<Enchantment> by lazy { world.registryAccess().registryOrThrow(Registries.ENCHANTMENT) }

    val biomeName: ResourceLocation
        get() = this.biomeRegistry.getKey(biome)!!

    private val struct = VariableStruct()
    private var structCompiled = false

    class StructureChunkCache {
        val missingTags = mutableSetOf<TagKey<Structure>>()
        val foundTags = mutableSetOf<TagKey<Structure>>()

        val foundIdentifiers = mutableSetOf<ResourceLocation>()

        var loadedStructures = false
        val structures = mutableSetOf<Holder<Structure>>()

        fun loadStructures(structureAccess: StructureManager, pos: BlockPos) {
            val registry = structureAccess.registryAccess().registryOrThrow(Registries.STRUCTURE)
            structureAccess.startsForStructure(ChunkPos(pos)) { structure ->
                val entry = registry.wrapAsHolder(structure)
                structures.add(entry)
                foundIdentifiers.add(entry.unwrapKey().get().location())
                false
            }
            loadedStructures = true
        }

        fun check(structureAccess: StructureManager, pos: BlockPos, tagKey: TagKey<Structure>): Boolean {

            if (!loadedStructures) {
                loadStructures(structureAccess, pos)
            }

            if (tagKey in missingTags) {
                return false
            } else if (tagKey in foundTags) {
                return true
            }

            structures.forEach { structure ->
                if (structure.`is`(tagKey)) {
                    foundTags.add(tagKey)
                    return true
                }
            }

            missingTags.add(tagKey)

            return false
        }

        fun check(structureAccess: StructureManager, pos: BlockPos, id: ResourceLocation): Boolean {
            if (!loadedStructures) {
                loadStructures(structureAccess, pos)
            }

            return id in foundIdentifiers
        }
    }

    /**
     * Filters a spawning detail by some extra condition defined by the context itself. This is for API purposes.
     * @return true if the [SpawnDetail] is acceptable by the context's own logic.
     */
    open fun preFilter(detail: SpawnDetail): Boolean {
        /** Returns true if none of the influences.affectSpawnable return false */
        return allForInfluences { it.affectSpawnable(detail, this) }
    }

    /**
     * Filters a spawning detail by some extra, more expensive condition defined by the context itself.
     * @return true if the [SpawnDetail] is acceptable by the context's own logic.
     */
    open fun postFilter(detail: SpawnDetail): Boolean = true

    open fun afterSpawn(entity: Entity) {
        applyInfluences { it.affectSpawn(entity) }
    }

    open fun getWeight(detail: SpawnDetail): Float {
        var weight = detail.weight
        applyInfluences(extraInfluences = detail.weightMultipliers) { weight = it.affectWeight(detail, this, weight) }
        return weight
    }

    fun getOrSetupStruct(): VariableStruct {
        if (structCompiled) {
            return struct
        }

        struct.setDirectly("light", DoubleValue(light.toDouble()))
        struct.setDirectly("x", DoubleValue(position.x.toDouble()))
        struct.setDirectly("y", DoubleValue(position.y.toDouble()))
        struct.setDirectly("z", DoubleValue(position.z.toDouble()))
        struct.setDirectly("moon_phase", DoubleValue(moonPhase.toDouble()))
        struct.setDirectly("world", ObjectValue(world.registryAccess().registryOrThrow(Registries.DIMENSION).wrapAsHolder(world)))
        struct.setDirectly("biome", ObjectValue(biomeRegistry.wrapAsHolder(biome)))

        structCompiled = true
        return struct
    }

    /**
     * Gets all influences that apply to this context, including the cause, and does something with them.
     *
     * Technically we could simple make a list of influences + cause and run that, but influences MUST be able
     * to run extremely frequently in performance-critical code, so we don't want to have to allocate a list.
     */
    fun applyInfluences(extraInfluences: List<SpawningInfluence>? = null, usage: (SpawningInfluence) -> Unit) {
        influences.forEach(usage)
        usage(cause)
        extraInfluences?.forEach(usage)
    }

    /**
     * Iterates through every operable influence to find if any match the predicate.
     */
    fun anyForInfluences(extraInfluences: List<SpawningInfluence>? = null, usage: (SpawningInfluence) -> Boolean): Boolean {
        if (influences.any(usage)) {
            return true
        }
        if (usage(cause)) {
            return true
        }
        extraInfluences?.forEach {
            if (usage(it)) {
                return true
            }
        }
        return false
    }

    fun allForInfluences(extraInfluences: List<SpawningInfluence>? = null, usage: (SpawningInfluence) -> Boolean): Boolean {
        if (influences.all(usage) && usage(cause)) {
            if (extraInfluences != null) {
                return extraInfluences.all(usage)
            }
            return true
        }
        return false
    }
}