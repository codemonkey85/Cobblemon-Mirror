package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class SaccharineStrippedLogBlock(properties: Properties) : RotatedPillarBlock(properties) {

    private var owner: LivingEntity? = null

    init {
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AGE, MIN_AGE)
                        .setValue(AXIS, Direction.Axis.Y)
        )
    }

    override fun setPlacedBy(
            level: Level,
            pos: BlockPos,
            state: BlockState,
            placer: LivingEntity?,
            itemStack: ItemStack
    ) {
        owner = placer
        super.setPlacedBy(level, pos, state, placer, itemStack)
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return state.getValue(AGE) < MAX_AGE
    }


    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(5) == 0 && owner == null) {
            val currentAge = state.getValue(AGE)
            if (currentAge < MAX_AGE) {
                level.setBlock(pos, state.setValue(AGE, currentAge + 1), 2)
            }
        }
    }

    override fun getShape(
            state: BlockState,
            level: BlockGetter,
            pos: BlockPos,
            context: CollisionContext
    ): VoxelShape {
        return SHAPE
    }

    override fun getCollisionShape(
            state: BlockState,
            level: BlockGetter,
            pos: BlockPos,
            context: CollisionContext
    ): VoxelShape {
        if (context is EntityCollisionContext && (context.entity as? ItemEntity)?.item?.`is`(CobblemonItemTags.APRICORNS) == true) {
            return Shapes.empty()
        }
        return super.getCollisionShape(state, level, pos, context)
    }

    override fun updateShape(
            state: BlockState,
            direction: Direction,
            neighborState: BlockState,
            level: LevelAccessor,
            pos: BlockPos,
            neighborPos: BlockPos
    ): BlockState {
        return if (!state.canSurvive(level, pos)) {
            Blocks.AIR.defaultBlockState()
        } else {
            super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE, AXIS)
    }

    override fun isPathfindable(
            state: BlockState,
            type: PathComputationType
    ): Boolean = false


    override fun useItemOn(
            stack: ItemStack,
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hand: InteractionHand,
            hit: BlockHitResult
    ): ItemInteractionResult {
        val itemStack = player.getItemInHand(hand)
        if (itemStack.`is`(Items.GLASS_BOTTLE) && state.getValue(AGE) == 1) {
            if (!player.isCreative) {
                itemStack.shrink(1)
            }
            player.addItem(CobblemonItems.SWEET_SAP.defaultInstance)
            level.setBlock(pos, state.setValue(AGE, 0), 2)
        }
        if (state.getValue(AGE) != MAX_AGE) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit)
        }
        return ItemInteractionResult.SUCCESS
    }

    companion object {
        val AGE: IntegerProperty = BlockStateProperties.AGE_1
        val AXIS: EnumProperty<Direction.Axis> = BlockStateProperties.AXIS
        const val MAX_AGE = 1
        const val MIN_AGE = 0

        private val SHAPE: VoxelShape = Shapes.block()
    }
}
