/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.SectionPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth.ceil
import net.minecraft.util.Mth.floor
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

fun Level.playSoundServer(
    position: Vec3,
    sound: SoundEvent,
    category: SoundSource = SoundSource.NEUTRAL,
    volume: Float = 1F,
    pitch: Float = 1F
) = (this as ServerLevel).playSound(null, position.x, position.y, position.z, sound, category, volume, pitch)

fun <T : ParticleOptions> Level.sendParticlesServer(
    particleType: T,
    position: Vec3,
    particles: Int,
    offset: Vec3,
    speed: Double
) = (this as ServerLevel).sendParticles(particleType, position.x, position.y, position.z, particles, offset.x, offset.y, offset.z, speed)

fun Level.squeezeWithinBounds(pos: BlockPos): BlockPos {
    val border = worldBorder
    return BlockPos(
        pos.x.coerceIn(border.minX.toInt(), border.maxX.toInt()),
        pos.y.coerceIn(minBuildHeight, maxBuildHeight),
        pos.z.coerceIn(border.minZ.toInt(), border.maxZ.toInt())
    )
}

fun ServerLevel.isBoxLoaded(box: AABB): Boolean {
    val startChunkX = SectionPos.posToSectionCoord(box.minX)
    val startChunkZ = SectionPos.posToSectionCoord(box.minZ)
    val endChunkX = SectionPos.posToSectionCoord(box.maxX)
    val endChunkZ = SectionPos.posToSectionCoord(box.maxZ)

    for (chunkX in startChunkX..endChunkX) {
        for (chunkZ in startChunkZ..endChunkZ) {
            if (!this.areEntitiesLoaded(ChunkPos.asLong(chunkX, chunkZ))) {
                return false
            }
        }
    }

    return true
}

fun AABB.getRanges(): Triple<IntRange, IntRange, IntRange> {
    return Triple(floor(minX)..ceil(maxX), minY.toInt()..ceil(maxY), minZ.toInt()..ceil(maxZ))
}

fun BlockGetter.doForAllBlocksIn(box: AABB, useMutablePos: Boolean, action: (BlockState, BlockPos) -> Unit) {
    val mutable = BlockPos.MutableBlockPos()
    val (xRange, yRange, zRange) = box.getRanges()
    for (x in xRange) {
        for (y in yRange) {
            for (z in zRange) {
                val pos = if (useMutablePos) mutable.set(x, y, z) else BlockPos(x, y, z)
                val state = getBlockState(pos)
                action(state, pos)
            }
        }
    }
}

fun BlockGetter.getBlockStates(box: AABB): Iterable<BlockState> {
    val states = mutableListOf<BlockState>()
    doForAllBlocksIn(box, useMutablePos = true) { state, _ -> states.add(state) }
    return states
}

fun BlockGetter.getBlockStatesWithPos(box: AABB): Iterable<Pair<BlockState, BlockPos>> {
    val states = mutableListOf<Pair<BlockState, BlockPos>>()
    doForAllBlocksIn(box, useMutablePos = true) { state, pos -> states.add(state to pos) }
    return states
}

fun BlockGetter.getWaterAndLavaIn(box: AABB): Pair<Boolean, Boolean> {
    var hasWater = false
    var hasLava = false

    doForAllBlocksIn(box, useMutablePos = true) { state, _ ->
        if (!hasWater && state.fluidState.`is`(FluidTags.WATER)) {
            hasWater = true
        }
        if (!hasLava && state.fluidState.`is`(FluidTags.LAVA)) {
            hasLava = true
        }
    }

    return hasWater to hasLava
}

fun Entity.canFit(pos: BlockPos) = canFit(pos.toVec3d())

fun Entity.canFit(vec: Vec3): Boolean {
    val box = boundingBox.move(vec.subtract(this.position()))
    return level().noCollision(box)
}

val Level.itemRegistry: Registry<Item>
    get() = registryAccess().registryOrThrow(Registries.ITEM)
val Level.biomeRegistry: Registry<Biome>
    get() = registryAccess().registryOrThrow(Registries.BIOME)
val Level.worldRegistry: Registry<Level>
    get() = registryAccess().registryOrThrow(Registries.DIMENSION)
val Level.enchantmentRegistry: Registry<Enchantment>
    get() = registryAccess().registryOrThrow(Registries.ENCHANTMENT)


fun Vec3.traceDownwards(
    world: Level,
    maxDistance: Float = 10F,
    stepDistance: Float = 0.5F,
): TraceResult? {
    var step = stepDistance
    val startPos = Vec3(x, y, z)
    val direction = Vec3(0.0, -1.0, 0.0)

    var lastBlockPos = startPos.toBlockPos()

    while (step <= maxDistance) {
        val location = startPos.add(direction.scale(step.toDouble()))
        step += stepDistance

        val blockPos = location.toBlockPos()

        if (blockPos == lastBlockPos) {
            continue
        } else {
            lastBlockPos = blockPos
        }

        val block = world.getBlockState(blockPos)
        if (!block.isAir) {
            val dir = findDirectionForIntercept(startPos, location, blockPos)
            return TraceResult(
                location = location,
                blockPos = blockPos,
                direction = dir
            )
        }
    }

    return null
}