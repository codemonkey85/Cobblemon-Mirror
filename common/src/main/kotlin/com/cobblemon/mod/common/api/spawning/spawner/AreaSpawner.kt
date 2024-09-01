/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.context.AreaContextResolver
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.AreaSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.prioritizedAreaCalculators
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.prospecting.SpawningProspector
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.isBoxLoaded
import com.cobblemon.mod.common.util.squeezeWithinBounds
import com.cobblemon.mod.common.util.toVec3f
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

/**
 * A type of [TickingSpawner] that operates within some area. When this spawner type
 * is told to do a spawning action, the subclass can provide a [SpawningArea] to use.
 * If a non-null value is returned, the [prospector] and [resolver] will be used to
 * select a spawn and action it.
 *
 * Subclasses must implement the function retrieving what area to do the spawning in,
 * but otherwise this class is feature-complete.
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
abstract class AreaSpawner(
    name: String,
    spawns: SpawnPool,
    manager: SpawnerManager
) : TickingSpawner(name, spawns, manager) {
    abstract fun getArea(cause: SpawnCause): SpawningArea?

    companion object {
        const val CHUNK_REACH = 3
    }

    var prospector: SpawningProspector = Cobblemon.prospector
    var resolver: AreaContextResolver = Cobblemon.areaContextResolver
    var contextCalculators: List<AreaSpawningContextCalculator<*>> = prioritizedAreaCalculators


    override fun run(cause: SpawnCause): Pair<SpawningContext, SpawnDetail>? {
        val area = getArea(cause)
        val constrainedArea = if (area != null) constrainArea(area) else null
        if (constrainedArea != null) {

            val areaBox = AABB.ofSize(
                Vec3(
                    constrainedArea.getCenter().toVec3f()
                ), CHUNK_REACH * 16.0 * 2, 1000.0, CHUNK_REACH * 16.0 * 2)
            if (!constrainedArea.world.isBoxLoaded(areaBox)) {
                return null
            }

            val numberNearby = constrainedArea.world.getEntitiesOfClass(
                PokemonEntity::class.java,
                areaBox,
                PokemonEntity::countsTowardsSpawnCap
            ).size

            val chunksCovered = CHUNK_REACH * CHUNK_REACH
            if (numberNearby.toFloat() / chunksCovered >= Cobblemon.config.pokemonPerChunk) {
                return null
            }

            //val prospectStart = System.currentTimeMillis()
            val slice = prospector.prospect(this, constrainedArea)
            //val prospectEnd = System.currentTimeMillis()
            val contexts = resolver.resolve(this, contextCalculators, slice)
            //val resolveEnd = System.currentTimeMillis()
            //val prospectDuration = prospectEnd - prospectStart
            //val resolveDuration = resolveEnd - prospectEnd
            //println("Prospecting took: $prospectDuration ms. Resolution took: $resolveDuration ms")
            // Takes about 3ms on my laptop to prospect, similar to context resolve - not very good, needs some thought
            return getSpawningSelector().select(this, contexts)
        }

        return null
    }

    fun isValidStartPoint(world: Level, chunk: ChunkAccess, startPos: BlockPos.MutableBlockPos): Boolean {
        val y = startPos.y
        if (!world.isLoaded(startPos) || !world.isLoaded(startPos.setY(y + 1))) {
            return false
        }

        val mid = chunk.getBlockState(startPos.setY(y))
        val above = chunk.getBlockState(startPos.setY(y + 1))

        // Above must be non-solid
         if (!above.isPathfindable(PathComputationType.AIR)) {
             return false
         }
        // Position must be non-air
        if (mid.isAir) {
            return false
        }

        return true
    }

    fun constrainArea(area: SpawningArea): SpawningArea? {
        val basePos = BlockPos.MutableBlockPos(area.baseX, area.baseY, area.baseZ)
        val originalY = area.baseY

        val (chunkX, chunkZ) = Pair(SectionPos.blockToSectionCoord(area.baseX), SectionPos.blockToSectionCoord(area.baseZ))

        // if the chunk isn't loaded, we don't want to go further & we don't want the getChunk function below to load/create the chunk.
        if (!area.world.areEntitiesLoaded(ChunkPos.asLong(chunkX, chunkZ))) return null

        val chunk = area.world.getChunk(chunkX, chunkZ, ChunkStatus.FULL) ?: return null

        var valid = isValidStartPoint(area.world, chunk, basePos)

        if (!valid) {
            var offset = 1
            do {
                if (isValidStartPoint(area.world, chunk, basePos.setY(originalY + offset))) {
                    valid = true
                    basePos.y = originalY + offset
                    break
                } else if (isValidStartPoint(area.world, chunk, basePos.setY(originalY - offset))) {
                    valid = true
                    basePos.y = originalY + offset
                    break
                }
                offset++
            } while (offset <= Cobblemon.config.maxVerticalCorrectionBlocks)
        }

        if (valid) {
            val min = area.world.squeezeWithinBounds(basePos)
            val max = area.world.squeezeWithinBounds(basePos.move(area.length, area.height, area.width))
            if (area.world.isLoaded(min) && area.world.isLoaded(max) &&
                min.x < max.x && min.y < max.y && min.z < max.z
            ) {
                return SpawningArea(
                    cause = area.cause,
                    world = area.world,
                    baseX = min.x,
                    baseY = min.y,
                    baseZ = min.z,
                    length = max.x - min.x,
                    height = max.y - min.y,
                    width = max.z - min.z
                )
            }
        }

        return null
    }
}
