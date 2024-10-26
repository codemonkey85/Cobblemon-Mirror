/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

//import dev.lambdaurora.lambdynlights.util.SodiumDynamicLightHandler.pos
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
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class BerryBlock(private val berryIdentifier: ResourceLocation, settings: Properties) : BaseEntityBlock(settings), BonemealableBlock, Mulchable {

    private val lookupDirections = setOf(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH)

    /**
     * Returns the [Berry] behind this block,
     * This will be null if it doesn't exist in the [Berries] registry.
     *
     * @return The [Berry] if existing.
     */
    fun berry(): Berry? = Berries.getByIdentifier(this.berryIdentifier)

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!player.isCreative && state.getValue(AGE) == FRUIT_AGE) {
            val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
            treeEntity.harvest(world, state, pos, player).forEach { drop -> Block.popResource(world, pos, drop) }
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = BerryBlockEntity(pos, state, berryIdentifier)

    override fun isValidBonemealTarget(world: LevelReader, pos: BlockPos, state: BlockState) = !this.isMaxAge(state)

    override fun isBonemealSuccess(world: Level, random: RandomSource, pos: BlockPos, state: BlockState) = !this.isMaxAge(state)

    override fun <T : BlockEntity> getTicker(world: Level, blockState: BlockState, blockWithEntityType: BlockEntityType<T>): BlockEntityTicker<T>? = createTickerHelper(blockWithEntityType, CobblemonBlockEntities.BERRY, BerryBlockEntity.TICKER)

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(WAS_GENERATED, false)
            .setValue(MULCH, MulchVariant.NONE)
            .setValue(AGE, 0)
            .setValue(IS_ROOTED, false))
    }

    override fun performBonemeal(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        growHelper(world, random, pos, state, true)
    }

    //grow, but cooler
    fun growHelper(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState, boneMealed: Boolean = false) {
        val berry = berry() ?: return
        if (boneMealed && random.nextFloat() > berry.boneMealChance) return
        val curAge = state.getValue(AGE)
        val newAge = curAge + 1
        if (newAge > FRUIT_AGE) return
        val newState = state.setValue(AGE, newAge)
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        if (curAge == MATURE_AGE) {
            treeEntity.generateGrowthPoints(world, newState, pos, null)
            determineMutation(world, random, pos, newState)
        }

        world.setBlock(pos, newState, Block.UPDATE_CLIENTS)
        convertMulchToEntity(world, newState, pos)
        treeEntity.goToNextStageTimer(FRUIT_AGE - curAge)
        treeEntity.setChanged()
    }

    fun determineMutation(world: Level, random: RandomSource, pos: BlockPos, state: BlockState) {
        val mutations = hashSetOf<Berry>()
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        for (direction in this.lookupDirections) {
            val redirectedPos = pos.offset(direction.normal)
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
        world: ServerLevel,
        pos: BlockPos,
        state: BlockState,
        variant: MulchVariant
    ): Boolean {
        val underBlockState = world.getBlockState(pos.below())
        val validSoil = state.getValue(IS_ROOTED) || underBlockState.`is`(CobblemonBlockTags.BERRY_SOIL)
        val treeEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return false
        return getMulch(treeEntity) == MulchVariant.NONE && state.getValue(AGE) < FLOWER_AGE && validSoil
    }

    override fun applyMulch(
        world: ServerLevel,
        random: RandomSource,
        pos: BlockPos,
        state: BlockState,
        variant: MulchVariant
    ) {
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        treeEntity.setMulch(variant, world, state, pos)
        world.playSound(null, pos, CobblemonSounds.MULCH_PLACE, SoundSource.BLOCKS, 0.6F, 1F)
    }

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        if (player.getItemInHand(InteractionHand.MAIN_HAND).item is ShovelItem && getMulch(treeEntity) != MulchVariant.NONE) {
            treeEntity.setChanged()
            world.playSound(null, pos, CobblemonSounds.MULCH_REMOVE, SoundSource.BLOCKS, 0.6F, 1F)
            this.spawnDestroyParticles(world, player, pos, state.setValue(AGE, 0))
            return InteractionResult.SUCCESS
        }

        if (player.getItemInHand(InteractionHand.MAIN_HAND).`is`(Items.BONE_MEAL) && !this.isMaxAge(state)) {
            return InteractionResult.PASS
        } else if (this.isMaxAge(state)) {
            val blockEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return InteractionResult.PASS
            blockEntity.harvest(world, state, pos, player).forEach { drop ->
                Block.popResource(world, pos, drop)
            }
            world.playSound(null, pos, CobblemonSounds.BERRY_HARVEST, SoundSource.BLOCKS, 0.4F, 1F)
            return InteractionResult.sidedSuccess(world.isClientSide)
        }
        return super.useWithoutItem(state, world, pos, player, blockHitResult)
    }

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val below = world.getBlockState(pos.below())
        return (state.getValue(WAS_GENERATED) && below.`is`(CobblemonBlockTags.BERRY_WILD_SOIL))
                || below.`is`(CobblemonBlockTags.BERRY_SOIL)
                || state.getValue(IS_ROOTED)
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    @Deprecated("Deprecated in Java")
    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, world: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (state.canSurvive(world, pos)) super.updateShape(state, direction, neighborState, world, pos, neighborPos) else Blocks.AIR.defaultBlockState()
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
//        if (!world.isClient) {
//            val blockEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return
//            blockEntity.generateGrowthPoints(world, state, pos, placer)
//        }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE)
        builder.add(WAS_GENERATED)
        builder.add(MULCH)
        builder.add(IS_ROOTED)
    }

    override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        val berryItem = this.berry()?.item() ?: return ItemStack.EMPTY
        return ItemStack(berryItem)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val berry = this.berry() ?: return Shapes.block()
        return when (state.getValue(AGE)) {
            0 -> PLANTED_SHAPE
            1 -> PLANTED_SHAPE
            2 -> berry.sproutShape
            else -> berry.matureShape
        }
    }

    private fun isMaxAge(state: BlockState) = state.getValue(AGE) == FRUIT_AGE

    @Deprecated("Deprecated in Java")
    override fun getRenderShape(blockState: BlockState) = RenderShape.MODEL

    companion object {
        val CODEC: MapCodec<BerryBlock> = RecordCodecBuilder.mapCodec { it.group(
            ResourceLocation.CODEC.fieldOf("berry").forGetter(BerryBlock::berryIdentifier),
            propertiesCodec()
        ).apply(it, ::BerryBlock) }

        const val MATURE_AGE = 3
        const val FLOWER_AGE = 4
        const val FRUIT_AGE = 5

        val AGE: IntegerProperty = IntegerProperty.create("age", 0, FRUIT_AGE)
        val MULCH: EnumProperty<MulchVariant> = EnumProperty.create("mulch", MulchVariant::class.java)
        val WAS_GENERATED: BooleanProperty = BooleanProperty.create("generated")
        val IS_ROOTED: BooleanProperty = BooleanProperty.create("rooted")
//        val PLANTED_SHAPE = VoxelShapes.union(
//            VoxelShapes.cuboid(0.3125, -0.0625, 0.3125, 0.6875, 0.0, 0.6875),
//            VoxelShapes.cuboid(0.375, 0.0, 0.375, 0.625, 0.0625, 0.625)
//        )
        val PLANTED_SHAPE = Shapes.box(0.0, -0.1, 0.0, 1.0, 0.25, 1.0)


        val STANDARD_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val STANDARD_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val SHORT_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 12.0, 16.0))
        val SHORT_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))

        val VOLCANO_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 6.0, 16.0))
        val VOLCANO_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))

        val NEST_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 6.0, 16.0))
        val NEST_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 20.0, 16.0))

        val FRILL_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 8.0, 16.0))
        val FRILL_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 14.0, 16.0))

        val BLOCK_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 17.0, 16.0))
        val BLOCK_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val PYRAMID_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 17.0, 16.0))
        val PYRAMID_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val TAIL_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val TAIL_MATURE = listOf(AABB(0.0, 1.0, 0.0, 16.0, 24.0, 16.0))

        val SWORD_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 5.0, 16.0))
        val SWORD_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val PLATFORM_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 7.0, 16.0))
        val PLATFORM_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 22.0, 16.0))

        val STAND_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 13.0, 16.0))
        val STAND_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val CONE_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val CONE_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 23.0, 16.0))

        val SQUAT_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 12.0, 16.0))
        val SQUAT_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 22.0, 16.0))

        val LANTERN_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 20.0, 16.0))
        val LANTERN_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val BOX_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val BOX_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))

        val BLOSSOM_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 4.0, 16.0))
        val BLOSSOM_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 6.0, 16.0))

        val LILYPAD_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 11.0, 16.0))
        val LILYPAD_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))

        val TALL_SPROUT = listOf(AABB(0.0, -1.0, 0.0, 16.0, 16.0, 16.0))
        val TALL_MATURE = listOf(AABB(0.0, -1.0, 0.0, 16.0, 24.0, 16.0))


        fun getMulch(entity: BerryBlockEntity) = entity.mulchVariant

        fun convertMulchToEntity(world: ServerLevel, state: BlockState, pos: BlockPos) {
            val entity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return
            if (state.getValue(MULCH) != MulchVariant.NONE && state.getValue(MULCH) != entity.mulchVariant) {
                entity.mulchVariant = state.getValue(MULCH)
                world.setBlockAndUpdate(pos, state.setValue(MULCH, MulchVariant.NONE))
            }
        }
    }
}