/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.chest

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.block.entity.GildedChestBlockEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.block.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.mob.PiglinBrain
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

@Suppress("OVERRIDE_DEPRECATION")
class GildedChestBlock(settings: Settings, val type: Type = Type.RED) : BlockWithEntity(settings), Waterloggable {

    init {
        defaultState = defaultState
            .with(Properties.HORIZONTAL_FACING, Direction.SOUTH)
            .with(WATERLOGGED, false)
    }

    companion object {
        val POKEMON_ARGS = "gimmighoul"
        val LEVEL_RANGE = 5..30
        val WATERLOGGED = BooleanProperty.of("waterlogged")

        val SOUTH_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.25, 1.0, 1.0, 0.9375)
        )
        val NORTH_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0625, 1.0, 1.0, 0.75)
        )
        val WEST_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.75, 1.0, 1.0)
        )
        val EAST_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.25, 0.0, 0.0, 0.9375, 1.0, 1.0)
        )
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = GildedChestBlockEntity(pos, state, type)

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return when (state.get(HorizontalFacingBlock.FACING)) {
            Direction.NORTH -> NORTH_OUTLINE
            Direction.SOUTH -> SOUTH_OUTLINE
            Direction.WEST -> WEST_OUTLINE
            else -> EAST_OUTLINE
        }
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        return super.getStateForNeighborUpdate(state,   direction, neighborState, world, pos, neighborPos)
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.get(WATERLOGGED)) {
            Fluids.WATER.getStill(false)
        } else super.getFluidState(state)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(Properties.HORIZONTAL_FACING)
        builder.add(WATERLOGGED)
    }

    private val facingToYaw: HashMap<Direction, Float> = hashMapOf(
        Direction.NORTH to -179.0F,
        Direction.WEST to 90.0F,
        Direction.SOUTH to 0.0F,
        Direction.EAST to -90.0F
    )

    override fun getName(): MutableText {
        return if (isFake()) Text.translatable("block.cobblemon.gilded_chest") else super.getName()
    }

    fun isFake() = (type == Type.FAKE)

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        if (!world.isClient) {
            if (isFake() && (player is ServerPlayerEntity)) {
                spawnPokemon(world, pos, state, player)
            }
            val bEntity = world.getBlockEntity(pos) as? GildedChestBlockEntity
            bEntity?.markRemoved()
        } else super.onBreak(world, pos, state, player)
    }

    private fun spawnPokemon(world: World, pos: BlockPos, state: BlockState, player: ServerPlayerEntity) : ActionResult {
        val properties = "$POKEMON_ARGS lvl=${LEVEL_RANGE.random()}"
        val pokemon = PokemonProperties.parse(properties)
        val entity = pokemon.createEntity(world)

        // The yaw based on the block's facing direction
        val yaw = facingToYaw[state[HorizontalFacingBlock.FACING]] ?: 0.0F

        entity.dataTracker.set(PokemonEntity.SPAWN_DIRECTION, facingToYaw[state[HorizontalFacingBlock.FACING]])
        val offsetDir = state[HorizontalFacingBlock.FACING]
        val vec = pos.toVec3d().add(offsetDir.offsetX * 0.1 + 0.5, 0.0, offsetDir.offsetZ * 0.1 + 0.5)
        entity.refreshPositionAndAngles(vec.x, vec.y, vec.z, yaw, entity.pitch)
        world.spawnEntity(entity)

        world.removeBlock(pos, false)
        afterOnServer(ticks = 2) {
            if (player !in player.world.players) {
                return@afterOnServer
            }
            val party = player.party()
            if (!player.isCreative) {
                entity.forceBattle(player)
            } else {
                world.playSound(null, pos, CobblemonSounds.GIMMIGHOUL_REVEAL, SoundCategory.NEUTRAL)
            }
        }
        return ActionResult.SUCCESS
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (isFake()) {
            if (player is ServerPlayerEntity) {
                return spawnPokemon(world, pos, state, player)
            } else {
                return ActionResult.SUCCESS
            }
        }
        val entity = world.getBlockEntity(pos) as? GildedChestBlockEntity ?: return ActionResult.FAIL
        if (world.getBlockState(pos.up()).isSolidBlock(world, pos.up())) return ActionResult.FAIL
        player.openHandledScreen(entity)
        if (!player.world.isClient) {
            PiglinBrain.onGuardedBlockInteracted(player, true)
        }
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!state.isOf(newState.block) && !world.isClient) {
            val chest = world.getBlockEntity(pos) as? GildedChestBlockEntity
            chest?.let {
                ItemScatterer.spawn(world, pos, chest.inventoryContents)
            }
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.ENTITYBLOCK_ANIMATED

    override fun getPlacementState(blockPlaceContext: ItemPlacementContext): BlockState? {
        return defaultState
            .with(HorizontalFacingBlock.FACING, blockPlaceContext.horizontalPlayerFacing.opposite)
            .with(WATERLOGGED, blockPlaceContext.world.getFluidState(blockPlaceContext.blockPos).fluid == Fluids.WATER)
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(
            Properties.HORIZONTAL_FACING, rotation.rotate(
                state.get(Properties.HORIZONTAL_FACING) as Direction
            )
        ) as BlockState
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean {
        return true
    }

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos?): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING) as Direction))
    }

    enum class Type(val poserId: Identifier) {
        RED(cobblemonResource("gilded_chest")),
        BLUE(cobblemonResource("blue_gilded_chest")),
        GREEN(cobblemonResource("green_gilded_chest")),
        PINK(cobblemonResource("pink_gilded_chest")),
        WHITE(cobblemonResource("white_gilded_chest")),
        BLACK(cobblemonResource("black_gilded_chest")),
        YELLOW(cobblemonResource("yellow_gilded_chest")),
        FAKE(cobblemonResource("gilded_chest"))
    }

    @Deprecated("Deprecated in Java")
    override fun canPathfindThrough(state: BlockState?, world: BlockView?, pos: BlockPos?, type: NavigationType?): Boolean {
        return false
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        val blockEntity = world.getBlockEntity(pos)
        if (itemStack.hasCustomName() && blockEntity is GildedChestBlockEntity) {
            blockEntity.customName = itemStack.name
        }
    }

    override fun scheduledTick(
        state: BlockState?,
        world: ServerWorld?,
        pos: BlockPos?,
        random: Random?
    ) {
        val blockEntity = world?.getBlockEntity(pos) as? GildedChestBlockEntity ?: return
        blockEntity.onScheduledTick()
    }

}