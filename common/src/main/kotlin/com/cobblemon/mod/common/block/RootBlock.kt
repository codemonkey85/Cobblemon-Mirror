/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.world.BigRootPropagatedEvent
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent

@Suppress("OVERRIDE_DEPRECATION", "UNUSED_PARAMETER", "MemberVisibilityCanBePrivate", "DEPRECATION")
abstract class RootBlock(settings: Properties) : Block(settings), BonemealableBlock, ShearableBlock {
    private val possibleDirections = setOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
    override fun isRandomlyTicking(state: BlockState) = true

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        // Check for propagation
        if (random.nextDouble() < Cobblemon.config.bigRootPropagationChance && world.getMaxLocalRawBrightness(pos) < MAX_PROPAGATING_LIGHT_LEVEL && !hasReachedSpreadCap(world, pos)) {
            this.spreadFrom(world, pos, random)
        }
    }

    fun hasReachedSpreadCap(world: Level, pos: BlockPos): Boolean {
        var nearby = 0
        val nearbyPositions = BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4)).iterator()
        while (nearbyPositions.hasNext()) {
            val blockPos = nearbyPositions.next()
            if (world.getBlockState(blockPos).`is`(CobblemonBlockTags.ROOTS)) {
                nearby++
                if (nearby >= Cobblemon.config.maxRootsInArea) {
                    return true
                }
            }
        }
        return false
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean = this.canGoOn(state, world, pos) { true }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == Direction.UP && !this.canSurvive(state, world, pos)) Blocks.AIR.defaultBlockState() else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun isValidBonemealTarget(world: LevelReader, pos: BlockPos, state: BlockState) = this.canSpread(world, pos, state)

    override fun isBonemealSuccess(world: Level, random: RandomSource, pos: BlockPos, state: BlockState) = this.canSpread(world, pos, state)

    override fun performBonemeal(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        this.spreadFrom(world, pos, random)
    }

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    fun spreadFrom(world: ServerLevel, pos: BlockPos, random: RandomSource) {
        val possibleDirections = this.possibleDirections.toMutableSet()
        while (possibleDirections.isNotEmpty()) {
            val picked = possibleDirections.random()
            possibleDirections.remove(picked)
            val adjacent = pos.relative(picked)
            if (this.canSpreadTo(this.defaultBlockState(), world, adjacent)) {
                val resultingSpread = this.spreadingRoot(random)
                val event = BigRootPropagatedEvent(
                    world = world,
                    pos = pos,
                    newRootPosition = adjacent,
                    resultingSpread = resultingSpread
                )
                CobblemonEvents.BIG_ROOT_PROPAGATED.postThen(
                    event = event,
                    ifSucceeded = { ev ->
                        world.setBlockAndUpdate(
                            ev.newRootPosition,
                            ev.resultingSpread
                        )
                    }
                )
                break
            }
        }
    }

    override fun attemptShear(world: Level, state: BlockState, pos: BlockPos, successCallback: () -> Unit): Boolean {
        // We always allow the shearing at the moment but hey if it ever changes at least it's easy to do so.
        world.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1F, 1F)
        world.setBlockAndUpdate(pos, this.shearedResultingState())
        val shearedDrop = this.shearedDrop()
        if (!shearedDrop.isEmpty) {
            popResource(world, pos, shearedDrop)
        }
        world.gameEvent(null, GameEvent.SHEAR, pos)
        return true
    }

    /**
     * Checks if this block can spread to any neighbouring blocks.
     *
     * @param world The [LevelReader] being queried.
     * @param pos The [BlockPos] the block is currently on.
     * @param state The [BlockState] of the block.
     * @return If any direction supports spreading.
     */
    protected fun canSpread(world: LevelReader, pos: BlockPos, state: BlockState): Boolean = this.possibleDirections.any { direction ->
        val adjacent = pos.relative(direction)
        this.canSpreadTo(this.defaultBlockState(), world, adjacent)
    }

    /**
     * Checks if the given coordinates allow for a root to spread to.
     *
     * @param state The base root [BlockState].
     * @param world The [LevelReader] being queried.
     * @param pos The [BlockPos] being queried.
     * @return If the given coordinates allow for a root to spread onto.
     */
    protected fun canSpreadTo(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val existingState = world.getBlockState(pos)
        // Isn't this useless since it's always air or replaceable if this is happening, not! See the spread implementation for the need of a valid block in the target pos as well.
        return (existingState.isAir || existingState.canBeReplaced()) && this.canGoOn(state, world, pos) { ceiling -> ceiling.`is`(CobblemonBlockTags.ROOTS_SPREADABLE) }
    }

    /**
     * Picks the [BlockState] that results from a spread of this block.
     *
     * @param random The [Random] instance used during a spread attempt.
     * @return The [BlockState] that will represent the spread root.
     */
    protected fun spreadingRoot(random: RandomSource): BlockState =
        if (random.nextFloat() < Cobblemon.config.energyRootChance) CobblemonBlocks.ENERGY_ROOT.defaultBlockState()
        else this.defaultBlockState()

    /**
     * Checks if the given coordinates allow for a root to be placed with some context.
     *
     * @param state The base root [BlockState].
     * @param world The [LevelReader] being queried.
     * @param pos The [BlockPos] being queried.
     * @param ceilingValidator An extra condition to validate if the block it will be placed on allows for it based on context.
     * @return If the given coordinates allow for a root to be set.
     */
    protected fun canGoOn(state: BlockState, world: LevelReader, pos: BlockPos, ceilingValidator: (ceiling: BlockState) -> Boolean): Boolean {
        val up = pos.above()
        val upState = world.getBlockState(up)
        return upState.isFaceSturdy(world, up, Direction.DOWN) && ceilingValidator(upState) // todo (techdaan): ensure this is the right mapping
    }

    /**
     * Resolves the [BlockState] when sheared.
     *
     * @return The resulting [BlockState] when this [ShearableBlock] is sheared.
     */
    protected abstract fun shearedResultingState(): BlockState

    /**
     * Resolves the [ItemStack] drop when sheared.
     *
     * @return The resulting [ItemStack] to be dropped when this [ShearableBlock] is sheared.
     */
    protected abstract fun shearedDrop(): ItemStack

    companion object {
        const val MAX_PROPAGATING_LIGHT_LEVEL = 11
    }
}