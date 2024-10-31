/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pasture.PastureLink
import com.cobblemon.mod.common.api.pasture.PastureLinkManager
import com.cobblemon.mod.common.api.pasture.PasturePermissionControllers
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.util.isInBattle
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.util.voxelShape
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
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
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.*
import net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED
import net.minecraft.world.level.gameevent.GameEvent

@Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
class PastureBlock(settings: Properties): BaseEntityBlock(settings), SimpleWaterloggedBlock, PreEmptsExplosion {
    companion object {
        val CODEC = simpleCodec(::PastureBlock)

        val PART: EnumProperty<PasturePart> = EnumProperty.create("part", PasturePart::class.java)
        val ON: BooleanProperty = BooleanProperty.create("on")

        private val SOUTH_AABB_TOP = buildCollider(top = true, Direction.NORTH)
        private val NORTH_AABB_TOP = buildCollider(top = true, Direction.SOUTH)
        private val WEST_AABB_TOP = buildCollider(top = true, Direction.WEST)
        private val EAST_AABB_TOP = buildCollider(top = true, Direction.EAST)

        private val SOUTH_AABB_BOTTOM = buildCollider(top = false, Direction.SOUTH)
        private val NORTH_AABB_BOTTOM = buildCollider(top = false, Direction.NORTH)
        private val WEST_AABB_BOTTOM = buildCollider(top = false, Direction.WEST)
        private val EAST_AABB_BOTTOM = buildCollider(top = false, Direction.EAST)

        private fun buildCollider(top: Boolean, direction: Direction): VoxelShape {
            if (top) {
                return Shapes.or(
                    voxelShape(0.1875, 0.0, 0.0625, 0.8125, 0.0625, 0.3125, direction),
                    voxelShape(0.125, 0.0, 0.375, 0.875, 0.1875, 0.9375, direction),
                    voxelShape(0.1875, 0.1875, 0.4375, 0.8125, 0.6875, 0.9375, direction),
                    voxelShape(0.8125, 0.1875, 0.375, 0.875, 0.6875, 0.9375, direction),
                    voxelShape(0.125, 0.1875, 0.375, 0.1875, 0.6875, 0.9375, direction),
                    voxelShape(0.125, 0.6875, 0.375, 0.875, 0.75, 0.9375, direction)
                )
            } else {
                return Shapes.or(
                    voxelShape(0.875, 0.0, 0.0, 1.0, 1.0, 0.125, direction),
                    voxelShape(0.125, 0.0, 0.0, 0.875, 0.125, 0.125, direction),
                    voxelShape(0.125, 0.875, 0.0, 0.875, 1.0, 0.125, direction),
                    voxelShape(0.125, 0.125, 0.0625, 0.875, 0.875, 0.125, direction),
                    voxelShape(0.0625, 0.125, 0.125, 0.125, 0.875, 0.875, direction),
                    voxelShape(0.0, 0.0, 0.0, 0.125, 1.0, 0.125, direction),
                    voxelShape(0.125, 0.0, 0.125, 0.875, 0.125, 1.0, direction),
                    voxelShape(0.875, 0.125, 0.125, 0.9375, 0.875, 0.875, direction),
                    voxelShape(0.125, 0.875, 0.125, 0.875, 1.0, 1.0, direction),
                    voxelShape(0.875, 0.0, 0.875, 1.0, 1.0, 1.0, direction),
                    voxelShape(0.875, 0.0, 0.125, 1.0, 0.125, 0.875, direction),
                    voxelShape(0.875, 0.875, 0.125, 1.0, 1.0, 0.875, direction),
                    voxelShape(0.0, 0.875, 0.125, 0.125, 1.0, 0.875, direction),
                    voxelShape(0.0, 0.0, 0.125, 0.125, 0.125, 0.875, direction),
                    voxelShape(0.0, 0.0, 0.875, 0.125, 1.0, 1.0, direction),
                    voxelShape(0.0, 0.125, 0.375, 0.0625, 0.875, 0.625, direction),
                    voxelShape(0.9375, 0.125, 0.375, 1.0, 0.875, 0.625, direction),
                    voxelShape(0.1875, 0.1875, 0.05625, 0.8125, 0.75, 0.05625, direction),
                    voxelShape(0.1875, 0.125, 0.3125, 0.8125, 0.3125, 0.875, direction),
                    voxelShape(0.1875, 0.125, 0.3125, 0.8125, 0.3125, 0.875, direction),
                    voxelShape(0.1875, 0.0625, 0.875, 0.8125, 0.25, 0.875, direction),
                    voxelShape(0.1875, 0.25, 0.25, 0.1875, 0.4375, 0.875, direction),
                    voxelShape(0.8125, 0.25, 0.25, 0.8125, 0.4375, 0.875, direction),
                    voxelShape(0.1875, 0.3125, 0.3125, 0.8125, 0.5, 0.3125, direction),
                    voxelShape(0.25, 0.75, 0.3125, 0.75, 1.0, 0.8125, direction),
                    // I'm lazy and this gets optimized in union anyway
                    voxelShape(0.0, 0.0, 0.0, 0.0625, 1.0, 1.0, direction),
                    voxelShape(0.9375, 0.0, 0.0, 1.0, 1.0, 1.0, direction),
                )
            }
        }
    }

    enum class PasturePart(private val label: String) : StringRepresentable {
        TOP("top"),
        BOTTOM("bottom");
        override fun getSerializedName(): String = label
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState) = if (blockState.getValue(PART) == PasturePart.BOTTOM) PokemonPastureBlockEntity(blockPos, blockState) else null


    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(PART, PasturePart.BOTTOM)
            .setValue(ON, false))
    }

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL
    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState? {
        val abovePosition = blockPlaceContext.clickedPos.above()
        val world = blockPlaceContext.level
        if (world.getBlockState(abovePosition).canBeReplaced(blockPlaceContext) && !world.isOutsideBuildHeight(abovePosition)) {
            return defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.horizontalDirection)
                .setValue(PART, PasturePart.BOTTOM)
                .setValue(WATERLOGGED, blockPlaceContext.level.getFluidState(blockPlaceContext.clickedPos).type == Fluids.WATER)
        }

        return null
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        return true
    }

    fun getPositionOfOtherPart(state: BlockState, pos: BlockPos): BlockPos {
        return if (state.hasProperty(PART) && state.getValue(PART) == PasturePart.BOTTOM) {
            pos.above()
        } else {
            pos.below()
        }
    }

    fun getBasePosition(state: BlockState, pos: BlockPos): BlockPos {
        return if (isBase(state)) {
            pos
        } else {
            pos.below()
        }
    }

    private fun isBase(state: BlockState): Boolean = state.hasProperty(PART) && state.getValue(PART) == PasturePart.BOTTOM

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun isPathfindable(
        blockState: BlockState,
        pathComputationType: PathComputationType
    ): Boolean = false

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HorizontalDirectionalBlock.FACING)
        builder.add(PART)
        builder.add(ON)
        builder.add(WATERLOGGED)
    }

    fun checkBreakEntity(world: LevelAccessor, state: BlockState, pos: BlockPos) {
        if (state.getValue(PART) == PasturePart.TOP) {
            return
        }
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is PokemonPastureBlockEntity) {
            blockEntity.onBroken()
        }
    }

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        checkBreakEntity(world, state, pos)
        if (!world.isClientSide && player?.isCreative == true) {
            var blockPos: BlockPos = BlockPos.ZERO
            var blockState: BlockState = state
            val part = state.getValue(PART)
            if (part == PasturePart.TOP && world.getBlockState(pos.below().also { blockPos = it }).also { blockState = it }.`is`(state.block) && blockState.getValue(PART) == PasturePart.BOTTOM) {
                checkBreakEntity(world, blockState, blockPos)
                val blockState2 = if (blockState.fluidState.`is`(Fluids.WATER)) Blocks.WATER.defaultBlockState() else Blocks.AIR.defaultBlockState()
                world.setBlock(blockPos, blockState2, UPDATE_ALL or UPDATE_SUPPRESS_DROPS)
                world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, blockPos, getId(blockState))
            }
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    override fun whenExploded(world: Level, state: BlockState, pos: BlockPos) {
        val blockEntity = world.getBlockEntity(pos) as? PokemonPastureBlockEntity ?: return
        blockEntity.onBroken()
    }

    override fun <T : BlockEntity?> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return createTickerHelper(type, CobblemonBlockEntities.PASTURE, PokemonPastureBlockEntity.TICKER::tick)
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack?) {
        world.setBlock(
            pos.above(),
            state
                .setValue(PART, PasturePart.TOP)
                .setValue(WATERLOGGED, world.getFluidState((pos.above())).type == Fluids.WATER),
            3
        )
        world.blockUpdated(pos, Blocks.AIR)
        state.updateNeighbourShapes(world, pos, 3)

        if (world is ServerLevel && placer is ServerPlayer) {
            val blockEntity = world.getBlockEntity(pos) as? PokemonPastureBlockEntity ?: return
            blockEntity.ownerId = placer.uuid
            blockEntity.ownerName = placer.gameProfile.name
            blockEntity.setChanged()
        }
    }

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (player is ServerPlayer && !player.isInBattle()) {
            val basePos = getBasePosition(state, pos)

            // Remove any duplicate block entities that may exist
            world.getBlockEntity(basePos.above())?.setRemoved()

            val baseEntity = world.getBlockEntity(basePos)
            if (baseEntity !is PokemonPastureBlockEntity) {
                return InteractionResult.SUCCESS
            }

            val pcId = Cobblemon.storage.getPC(player).uuid
            val linkId = UUID.randomUUID()

            val perms = PasturePermissionControllers.permit(player, baseEntity)
            CobblemonNetwork.sendPacketToPlayer(
                player = player,
                packet = OpenPasturePacket(
                    pcId = pcId,
                    pastureId = linkId,
                    permissions = perms,
                    limit = baseEntity.getMaxTethered(),
                    tetheredPokemon = baseEntity.tetheredPokemon.mapNotNull { it.toDTO(player) }
                )
            )

            PastureLinkManager.createLink(player.uuid, PastureLink(linkId, pcId, ResourceLocation.tryParse(world.dimensionTypeRegistration().registeredName)!!, getBasePosition(state, pos), perms))

            world.playSoundServer(
                position = pos.toVec3d(),
                sound = CobblemonSounds.PC_ON,
                volume = 0.5F,
                pitch = 1F
            )
            world.gameEvent(player, GameEvent.BLOCK_OPEN, pos)
            return InteractionResult.SUCCESS
        }

        return InteractionResult.SUCCESS
    }

    override fun getShape(
        blockState: BlockState,
        blockGetter: BlockGetter,
        blockPos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape {
        return if (blockState.getValue(PART) == PasturePart.TOP)  {
            when (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
                Direction.SOUTH -> SOUTH_AABB_TOP
                Direction.WEST -> WEST_AABB_TOP
                Direction.EAST -> EAST_AABB_TOP
                else -> NORTH_AABB_TOP
            }
        } else {
            when (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
                Direction.SOUTH -> SOUTH_AABB_BOTTOM
                Direction.WEST -> WEST_AABB_BOTTOM
                Direction.EAST -> EAST_AABB_BOTTOM
                else -> NORTH_AABB_BOTTOM
            }
        }
    }

    override fun rotate(blockState: BlockState, rotation: Rotation): BlockState = blockState.setValue(
        HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(
            HorizontalDirectionalBlock.FACING)))

    override fun mirror(blockState: BlockState, mirror: Mirror): BlockState {
        return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)))
    }

    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block)) super.onRemove(state, world, pos, newState, moved)
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) {
            Fluids.WATER.getSource(false)
        } else super.getFluidState(state)
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world))
        }

        val isPasture = neighborState.`is`(this)
        val part = state.getValue(PART)
        if (!isPasture && part == PasturePart.TOP && neighborPos == pos.below()) {
            return Blocks.AIR.defaultBlockState()
        } else if (!isPasture && part == PasturePart.BOTTOM && neighborPos == pos.above()) {
            checkBreakEntity(world, state, pos)
            return Blocks.AIR.defaultBlockState()
        }

        return state
    }
}