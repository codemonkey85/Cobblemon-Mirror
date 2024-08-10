/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.berry.BerryMutationOfferEvent
import com.cobblemon.mod.common.api.events.berry.BerryMutationResultEvent
import com.cobblemon.mod.common.api.mulch.MulchVariant
import com.cobblemon.mod.common.api.mulch.Mulchable
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import dev.lambdaurora.lambdynlights.util.SodiumDynamicLightHandler.pos
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Blocks
import net.minecraft.block.FarmlandBlock
import net.minecraft.block.Fertilizable
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.ShovelItem
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

class BerryBlock(private val berryIdentifier: Identifier, settings: Settings) : BlockWithEntity(settings), Fertilizable, Mulchable {

    private val lookupDirections = setOf(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH)

    /**
     * Returns the [Berry] behind this block,
     * This will be null if it doesn't exist in the [Berries] registry.
     *
     * @return The [Berry] if existing.
     */
    fun berry(): Berry? = Berries.getByIdentifier(this.berryIdentifier)

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        if (!player.isCreative && state.get(AGE) == FRUIT_AGE) {
            val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
            treeEntity.harvest(world, state, pos, player).forEach { drop -> Block.dropStack(world, pos, drop) }
        }
        super.onBreak(world, pos, state, player)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BerryBlockEntity(pos, state, berryIdentifier)

    override fun isFertilizable(world: WorldView, pos: BlockPos, state: BlockState, isClient: Boolean) = !this.isMaxAge(state)

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState) = !this.isMaxAge(state)

    override fun <T : BlockEntity> getTicker(world: World, blockState: BlockState, blockWithEntityType: BlockEntityType<T>): BlockEntityTicker<T>? = checkType(blockWithEntityType, CobblemonBlockEntities.BERRY, BerryBlockEntity.TICKER)

    init {
        defaultState = this.stateManager.defaultState
            .with(WAS_GENERATED, false)
            .with(MULCH, MulchVariant.NONE)
            .with(AGE, 0)
            .with(IS_ROOTED, false)
    }

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        growHelper(world, random, pos, state, true)
    }

    //grow, but cooler
    fun growHelper(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState, boneMealed: Boolean = false) {
        val berry = berry() ?: return
        if (boneMealed && random.nextFloat() > berry.boneMealChance) return
        val curAge = state.get(AGE)
        val newAge = curAge + 1
        if (newAge > FRUIT_AGE) return
        val newState = state.with(AGE, newAge)
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        if (curAge == MATURE_AGE) {
            treeEntity.generateGrowthPoints(world, newState, pos, null)
            determineMutation(world, random, pos, newState)
        }

        world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS)
        convertMulchToEntity(world, newState, pos)
        treeEntity.goToNextStageTimer(FRUIT_AGE - curAge)
        treeEntity.markDirty()
    }

    fun determineMutation(world: World, random: Random, pos: BlockPos, state: BlockState) {
        val mutations = hashSetOf<Berry>()
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        for (direction in this.lookupDirections) {
            val redirectedPos = pos.add(direction.vector)
            val redirectedState = world.getBlockState(redirectedPos)
            val berryBlock = redirectedState.block as? BerryBlock ?: continue
            val berry = berryBlock.berry() ?: continue
            val mutation = this.berry()?.mutationWith(berry) ?: continue
            mutations += mutation
        }
        this.berry()?.let { berry ->
            CobblemonEvents.BERRY_MUTATION_OFFER.post(BerryMutationOfferEvent(berry, world, state, pos, mutations)) { berryMutationOffer ->
                if (berryMutationOffer.mutations.isNotEmpty()) {
                    var mutateChance = 125
                    if (getMulch(treeEntity) == MulchVariant.SURPRISE) {
                        mutateChance *= 4
                        treeEntity.decrementMulchDuration(world, pos, state)
                    }
                    val mutation = if (random.nextInt(1000) < mutateChance) mutations.random() else null
                    (world.getBlockEntity(pos) as? BerryBlockEntity)?.let { blockEntity ->
                        CobblemonEvents.BERRY_MUTATION_RESULT.post(BerryMutationResultEvent(berry, world, state, pos, berryMutationOffer.mutations, mutation)) { berryMutationResult ->
                            berryMutationResult.pickedMutation?.let { mutation -> blockEntity.mutate(mutation) }
                        }
                    }
                }
            }
        }
    }

    override fun canHaveMulchApplied(
        world: ServerWorld,
        pos: BlockPos,
        state: BlockState,
        variant: MulchVariant
    ): Boolean {
        val underBlockState = world.getBlockState(pos.down())
        val validSoil = state.get(IS_ROOTED) || underBlockState.isIn(CobblemonBlockTags.BERRY_SOIL)
        val treeEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return false
        return getMulch(treeEntity) == MulchVariant.NONE && state.get(AGE) < FLOWER_AGE && validSoil
    }

    override fun applyMulch(
        world: ServerWorld,
        random: Random,
        pos: BlockPos,
        state: BlockState,
        variant: MulchVariant
    ) {
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        treeEntity.setMulch(variant, world, state, pos)
        world.playSound(null, pos, CobblemonSounds.MULCH_PLACE, SoundCategory.BLOCKS, 0.6F, 1F)
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        if (player.getStackInHand(hand).item is ShovelItem && getMulch(treeEntity) != MulchVariant.NONE) {
            treeEntity.setMulch(MulchVariant.NONE, world, state, pos)
            world.playSound(null, pos, CobblemonSounds.MULCH_REMOVE, SoundCategory.BLOCKS, 0.6F, 1F)
            this.spawnBreakParticles(world, player, pos, state.with(AGE, 0))
            return ActionResult.SUCCESS
        }

        if (player.getStackInHand(hand).isOf(Items.BONE_MEAL) && !this.isMaxAge(state)) {
            return ActionResult.PASS
        } else if (this.isMaxAge(state)) {
            val blockEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return ActionResult.PASS
            blockEntity.harvest(world, state, pos, player).forEach { drop ->
                Block.dropStack(world, pos, drop)
            }
            world.playSound(null, pos, CobblemonSounds.BERRY_HARVEST, SoundCategory.BLOCKS, 0.4F, 1F)
            return ActionResult.success(world.isClient)
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    @Deprecated("Deprecated in Java")
    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val below = world.getBlockState(pos.down())
        return (state.get(WAS_GENERATED) && below.isIn(CobblemonBlockTags.BERRY_WILD_SOIL))
                || below.isIn(CobblemonBlockTags.BERRY_SOIL)
                || state.get(IS_ROOTED)
    }

    @Deprecated("Deprecated in Java")
    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (state.canPlaceAt(world, pos)) super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos) else Blocks.AIR.defaultState
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
//        if (!world.isClient) {
//            val blockEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return
//            blockEntity.generateGrowthPoints(world, state, pos, placer)
//        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE)
        builder.add(WAS_GENERATED)
        builder.add(MULCH)
        builder.add(IS_ROOTED)
    }

    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack {
        val berryItem = this.berry()?.item() ?: return ItemStack.EMPTY
        return ItemStack(berryItem)
    }

    @Deprecated("Deprecated in Java")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        val berry = this.berry() ?: return VoxelShapes.fullCube()
        return when (state.get(AGE)) {
            0 -> PLANTED_SHAPE
            1 -> PLANTED_SHAPE
            2 -> berry.sproutShape
            else -> berry.matureShape
        }
    }

    private fun isMaxAge(state: BlockState) = state.get(AGE) == FRUIT_AGE

    @Deprecated("Deprecated in Java")
    override fun getRenderType(blockState: BlockState) = BlockRenderType.MODEL

    companion object {
        const val MATURE_AGE = 3
        const val FLOWER_AGE = 4
        const val FRUIT_AGE = 5

        val AGE: IntProperty = IntProperty.of("age", 0, FRUIT_AGE)
        val MULCH: EnumProperty<MulchVariant> = EnumProperty.of("mulch", MulchVariant::class.java)
        val WAS_GENERATED: BooleanProperty = BooleanProperty.of("generated")
        val IS_ROOTED: BooleanProperty = BooleanProperty.of("rooted")
//        val PLANTED_SHAPE = VoxelShapes.union(
//            VoxelShapes.cuboid(0.3125, -0.0625, 0.3125, 0.6875, 0.0, 0.6875),
//            VoxelShapes.cuboid(0.375, 0.0, 0.375, 0.625, 0.0625, 0.625)
//        )
        val PLANTED_SHAPE = VoxelShapes.cuboid(0.0, -0.1, 0.0, 1.0, 0.25, 1.0)


        val STANDARD_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val STANDARD_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val SHORT_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 12.0, 16.0))
        val SHORT_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))

        val VOLCANO_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 6.0, 16.0))
        val VOLCANO_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))

        val NEST_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 6.0, 16.0))
        val NEST_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 20.0, 16.0))

        val FRILL_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 8.0, 16.0))
        val FRILL_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 14.0, 16.0))

        val BLOCK_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 17.0, 16.0))
        val BLOCK_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val PYRAMID_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 17.0, 16.0))
        val PYRAMID_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val TAIL_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val TAIL_MATURE = listOf(Box(0.0, 1.0, 0.0, 16.0, 24.0, 16.0))

        val SWORD_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 5.0, 16.0))
        val SWORD_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val PLATFORM_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 7.0, 16.0))
        val PLATFORM_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 22.0, 16.0))

        val STAND_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 13.0, 16.0))
        val STAND_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val CONE_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val CONE_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 23.0, 16.0))

        val SQUAT_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 12.0, 16.0))
        val SQUAT_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 22.0, 16.0))

        val LANTERN_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 20.0, 16.0))
        val LANTERN_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val BOX_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val BOX_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val BLOSSOM_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 4.0, 16.0))
        val BLOSSOM_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 6.0, 16.0))

        val LILYPAD_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 11.0, 16.0))
        val LILYPAD_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))

        val TALL_SPROUT = listOf(Box(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val TALL_MATURE = listOf(Box(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))


        fun getMulch(entity: BerryBlockEntity) = entity.mulchVariant

        fun convertMulchToEntity(world: ServerWorld, state: BlockState, pos: BlockPos) {
            val entity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return
            if (state.get(MULCH) != MulchVariant.NONE && state.get(MULCH) != entity.mulchVariant) {
                entity.mulchVariant = state.get(MULCH)
                world.setBlockState(pos, state.with(MULCH, MulchVariant.NONE))
            }
        }
    }
}
