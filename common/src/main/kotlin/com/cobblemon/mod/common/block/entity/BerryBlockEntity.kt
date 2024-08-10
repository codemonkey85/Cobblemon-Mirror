/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.api.berry.GrowthPoint
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.berry.BerryHarvestEvent
import com.cobblemon.mod.common.api.mulch.MulchVariant
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.block.BerryBlock.Companion.getMulch
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent

class BerryBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CobblemonBlockEntities.BERRY, pos, state) {
    lateinit var berryIdentifier: Identifier
    private val ticksPerMinute = 1200
    var renderState: RenderState? = null
    //The time left for the tree until its either age 3 or age 5 (check block state, if we are at age 0<x<3 unti 3 otherwise until 5)
    var growthTimer: Int = 0
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("You cannot set the growth time to less than zero")
            }
            if (field != value) {
                this.markDirty()
            }
            field = value
        }
    //The time left for the tree to grow to the next stage
    var stageTimer: Int = 0
        set(value) {
            if (field != value) {
                this.markDirty()
            }
            field = value
        }
    private val growthPoints = arrayListOf<Identifier>()
    var mulchVariant = MulchVariant.NONE

    /**
     * The idea behind the growth point sequence is it's a 16-long string of
     * hexadecimal numbers. They represent the order of the growth points that
     * will get used by this tree. If the berry type has randomized growth points,
     * this sequence gets scrambled, and then when lining up berries with their
     * growth points, it will take the berry index, check the symbol at that
     * position in this sequence, and use that as the growth point index from
     * reference data.
     *
     * Kinda confusing but it works without saving an array or something. Protections
     * exist for when there are fewer than 16 growth points on a tree (currently always).
     */
    private var growthPointSequence = "0123456789ABCDEF"
    // Just a cheat to not invoke markDirty unnecessarily
    private var wasLoading = false
    var mulchDuration = 0
        set(value) {
            field = value
            markDirty()
        }

    constructor(pos: BlockPos, state: BlockState, berryIdentifier: Identifier): this(pos, state) {
        this.berryIdentifier = berryIdentifier
        resetGrowTimers(pos, state)
        if (state.get(BerryBlock.WAS_GENERATED) && state.get(BerryBlock.AGE) >= 4) {
            generateSimpleYields()
        }
    }

    fun decrementMulchDuration(world: World, pos: BlockPos, state: BlockState) {
        if (mulchVariant == MulchVariant.NONE || mulchVariant.duration == -1) {
            return
        }
        val newDuration = mulchDuration - 1
        mulchDuration = if (newDuration <= 0) {
            mulchVariant = MulchVariant.NONE
            this.markDirty()
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS)
            0
        } else {
            newDuration
        }
    }

    fun setMulch(mulch: MulchVariant, world: World, state: BlockState, pos: BlockPos) {
        this.mulchVariant = mulch
        this.mulchDuration = mulch.duration
        refreshTimers(pos)
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS)
        this.markDirty()
    }

    //This function basically runs twice when the berry tree is growing, once when it starts, once when it hits age 3
    //Why dont we just go 0-5? Design thinks its annoying to balance the numbers using 0-5 and 3-5
    fun resetGrowTimers(pos: BlockPos, state: BlockState) {
        val curAge = state.get(BerryBlock.AGE)
        if (curAge == 5) {
            return
        }

        val multiplier = 14
        val berry = Berries.getByIdentifier(berryIdentifier)!!
        val lowerGrowthLimit = (if (curAge == 0) berry.growthTime.first else berry.refreshRate.first) * multiplier / 10
        val upperGrowthLimit = (if (curAge == 0) berry.growthTime.last else berry.refreshRate.last) * multiplier / 10
        val growthRange = lowerGrowthLimit..upperGrowthLimit

        this.growthTimer = this.applyMulchModifier(pos, growthRange.random() * ticksPerMinute, true)
        val stagesLeft = if (curAge < 3) BerryBlock.MATURE_AGE - curAge else BerryBlock.FRUIT_AGE - curAge
        this.goToNextStageTimer(stagesLeft)
    }

    fun goToNextStageTimer(stagesLeft: Int) {
        val avgStageTime = growthTimer / stagesLeft
        //A number between 80% and 100% the average stage time
        //So stages have some variance
        stageTimer = this.world?.random?.nextBetween((avgStageTime * 8) / 10, avgStageTime) ?:
                (((Math.random() *  0.2) + 0.8) * avgStageTime).toInt()
        growthTimer -= stageTimer
    }

    /**
     * Used to apply an effect to the growth timer based on the mulch type.
     * Currently only used for growth mulch.
     * @param pos The position of the block
     * @param timer Timer to be modified
     * @return The modified timer
     */
    private fun applyMulchModifier(pos: BlockPos, timer: Int, decrementMulch: Boolean = false): Int {
        val state = world?.getBlockState(pos) ?: return timer

        val curAge = state.get(BerryBlock.AGE)
        if (curAge == 5) {
            return timer
        }

        if (mulchVariant != MulchVariant.GROWTH) {
            return timer
        }
        if (decrementMulch)  {
            world?.let {
                decrementMulchDuration(it, pos, state)
            }
        }
        return (timer * MulchVariant.GROWTH_TIME_MULTIPLIER).toInt()
    }

    /**
     * Used to refresh the timers when a new mulch is applied.
     * @param pos The position of the block
     */
    fun refreshTimers(pos: BlockPos) {
        this.growthTimer = this.applyMulchModifier(pos, growthTimer, false)
        this.stageTimer = this.applyMulchModifier(pos, stageTimer, false)
    }

    /**
     * Returns the [Berry] behind this entity,
     * This will be null if it doesn't exist in the [Berries] registry.
     *
     * @return The [Berry] if existing.
     */
    fun berry() = Berries.getByIdentifier(berryIdentifier)

    /**
     * Generates a random amount of growth points for this tree.
     *
     * @param world The [World] the tree is in.
     * @param state The [BlockState] of the tree.
     * @param pos The [BlockPos] of the tree.
     * @param placer The [LivingEntity] tending to the tree if any.
     */
    fun generateGrowthPoints(world: World, state: BlockState, pos: BlockPos, placer: LivingEntity?) {
        val berry = this.berry() ?: return
        val yield = berry.calculateYield(world, state, pos, placer)
        this.growthPoints.clear()
        repeat(yield) {
            this.growthPoints += berry.identifier
        }
        this.growthPointSequence = this.growthPointSequence.toCharArray().also { if (berry.randomizedGrowthPoints) it.shuffle() }.concatToString()
        this.markDirty()
    }

    //Used for naturally spawning berries
    fun generateSimpleYields() {
        val numBerries = berry()?.baseYield?.random() ?: return
        repeat(numBerries) {
            growthPoints.add(berryIdentifier)
        }
        this.growthPointSequence = this.growthPointSequence.toCharArray().also { if (berry()?.randomizedGrowthPoints != false) it.shuffle() }.concatToString()
    }

    /**
     * Calculates the berry produce in [ItemStack] form.
     *
     * @param world The [World] the tree is in.
     * @param state The [BlockState] of the tree.
     * @param pos The [BlockPos] of the tree.
     * @param player The [PlayerEntity] harvesting the tree.
     * @return The resulting [ItemStack]s to be dropped.
     */
    fun harvest(world: World, state: BlockState, pos: BlockPos, player: PlayerEntity): Collection<ItemStack> {
        val drops = arrayListOf<ItemStack>()
        val unique = this.growthPoints.groupingBy { it }.eachCount()
        unique.forEach { (identifier, amount) ->
            val berryItem = Berries.getByIdentifier(identifier)?.item()
            if (berryItem != null) {
                var remain = amount
                while (remain > 0) {
                    val count = remain.coerceAtMost(berryItem.maxCount)
                    drops += ItemStack(berryItem, count)
                    remain -= count
                }
            }
        }
        this.berry()?.let { berry ->
            if (player is ServerPlayerEntity) {
                CobblemonEvents.BERRY_HARVEST.post(BerryHarvestEvent(berry, player, world, pos, state, this, drops))
            }
        }
        refresh(world, pos, state, player)
        return drops
    }

    override fun readNbt(nbt: NbtCompound) {
        this.berryIdentifier = Identifier(nbt.getString(BERRY).takeIf { it.isNotBlank() } ?: "cobblemon:pecha")
        this.wasLoading = true
        this.growthPoints.clear()
        this.growthTimer = nbt.getInt(GROWTH_TIMER).coerceAtLeast(0)
        this.stageTimer = nbt.getInt(STAGE_TIMER).coerceAtLeast(0)
        //this.lifeCycles = nbt.getInt(LIFE_CYCLES).coerceAtLeast(0)
        nbt.getList(GROWTH_POINTS, NbtList.STRING_TYPE.toInt()).filterIsInstance<NbtString>().forEach { element ->
            // In case some 3rd party mutates the NBT incorrectly
            try {
                val identifier = Identifier(element.asString())
                this.growthPoints += identifier
            } catch (ignored: InvalidIdentifierException) {}
        }
        this.mulchDuration = nbt.getInt(MULCH_DURATION)
        this.wasLoading = false
        if (nbt.contains(GROWTH_POINTS_SEQUENCE)) {
            growthPointSequence = nbt.getString(GROWTH_POINTS_SEQUENCE)
        }
        if (nbt.contains(MULCH_VARIANT)) {
            mulchVariant = MulchVariant.valueOf(nbt.getString(MULCH_VARIANT))
        }
        this.renderState?.needsRebuild = true
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putInt(GROWTH_TIMER, this.growthTimer)
        nbt.putInt(STAGE_TIMER, this.stageTimer)
        val list = NbtList()
        list += this.growthPoints.map { NbtString.of(it.toString()) }
        nbt.put(GROWTH_POINTS, list)
        nbt.putString(BERRY, berryIdentifier.toString())
        nbt.putInt(MULCH_DURATION, mulchDuration)
        nbt.putString(GROWTH_POINTS_SEQUENCE, growthPointSequence)
        nbt.putString(MULCH_VARIANT, mulchVariant.toString())
    }

    override fun markDirty() {
        if (!this.wasLoading) {
            super.markDirty()
        }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return this.createNbt()
    }

    /**
     * Collects each [Berry] and [GrowthPoint].
     *
     * @return Collection of the [Berry] and [GrowthPoint].
     */
    internal fun berryAndGrowthPoint(): List<Pair<Berry, GrowthPoint>> {
        val baseBerry = this.berry() ?: return emptyList()
        val berryPoints = arrayListOf<Pair<Berry, GrowthPoint>>()
        val sequenceIndices = growthPointSequence.toCharArray().filter { it.digitToInt(16) < baseBerry.growthPoints.size }
        for ((index, identifier) in this.growthPoints.withIndex()) {
            val berry = Berries.getByIdentifier(identifier) ?: continue
            val sequenceIndexHex = sequenceIndices[index].digitToInt(16)
            berryPoints.add(berry to baseBerry.growthPoints[sequenceIndexHex])
        }
        return berryPoints
    }

    /**
     * Inserts the given [berry] as a mutation in a random [growthPoints].
     *
     * @param berry The [Berry] being mutated.
     */
    internal fun mutate(berry: Berry) {
        if (this.growthPoints.isEmpty()) {
            return
        }
        val index = this.growthPoints.indices.random()
        this.growthPoints[index] = berry.identifier
        this.markDirty()
    }

    private fun refresh(world: World, pos: BlockPos, state:BlockState, player: PlayerEntity) {
        val newState = state.with(BerryBlock.AGE, 3)
        world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS)
        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos)
        resetGrowTimers(pos, newState)
        return
    }

    //Using a similar approach to ComputerCraft, implement this client side
    interface RenderState : AutoCloseable {
        var needsRebuild: Boolean
    }

    companion object {
        internal val TICKER = BlockEntityTicker<BerryBlockEntity> { world, pos, state, blockEntity ->
            if (world.isClient) return@BlockEntityTicker
            if (state.get(BerryBlock.IS_ROOTED)) return@BlockEntityTicker
            if (blockEntity.stageTimer >= 0) {
                blockEntity.stageTimer--
            }
            if (blockEntity.stageTimer == 0) {
                (state.block as BerryBlock).growHelper(world as ServerWorld, world.random, pos, state)
            }
        }
        //private const val LIFE_CYCLES = "life_cycles"
        private const val GROWTH_POINTS = "GrowthPoints"
        private const val GROWTH_POINTS_SEQUENCE = "GrowthPointsSequence"
        private const val GROWTH_TIMER = "GrowthTimer"
        private const val STAGE_TIMER = "StageTimer"
        private const val BERRY = "Berry"
        private const val MULCH_DURATION = "MulchDuration"
        private const val MULCH_VARIANT = "MulchVariant"
    }

}
