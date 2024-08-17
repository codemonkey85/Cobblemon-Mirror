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
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION")
class GildedChestBlock(settings: Properties, val type: Type = Type.RED) : BaseEntityBlock(settings),
    SimpleWaterloggedBlock {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HORIZONTAL_FACING, Direction.SOUTH)
            .setValue(WATERLOGGED, false))
    }

    companion object {
        val CODEC: MapCodec<GildedChestBlock> = RecordCodecBuilder.mapCodec {
            it.group(
                propertiesCodec(),
                Type.CODEC.fieldOf("chestType").forGetter(GildedChestBlock::type)
            ).apply(it, ::GildedChestBlock)
        }

        val POKEMON_ARGS = "gimmighoul"
        val LEVEL_RANGE = 5..30

        val SOUTH_OUTLINE = Shapes.or(
            Shapes.box(0.0, 0.0, 0.25, 1.0, 1.0, 0.9375)
        )
        val NORTH_OUTLINE = Shapes.or(
            Shapes.box(0.0, 0.0, 0.0625, 1.0, 1.0, 0.75)
        )
        val WEST_OUTLINE = Shapes.or(
            Shapes.box(0.0625, 0.0, 0.0, 0.75, 1.0, 1.0)
        )
        val EAST_OUTLINE = Shapes.or(
            Shapes.box(0.25, 0.0, 0.0, 0.9375, 1.0, 1.0)
        )
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = GildedChestBlockEntity(pos, state, type)

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return when (state.getValue(HorizontalDirectionalBlock.FACING)) {
            Direction.NORTH -> NORTH_OUTLINE
            Direction.SOUTH -> SOUTH_OUTLINE
            Direction.WEST -> WEST_OUTLINE
            else -> EAST_OUTLINE
        }
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (state.getValue(WATERLOGGED)) world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world))
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) {
            Fluids.WATER.getSource(false)
        } else super.getFluidState(state)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(HORIZONTAL_FACING)
        builder.add(WATERLOGGED)
    }

    private val facingToYaw: HashMap<Direction, Float> = hashMapOf(
        Direction.NORTH to -179.0F,
        Direction.WEST to 90.0F,
        Direction.SOUTH to 0.0F,
        Direction.EAST to -90.0F
    )

    override fun getName(): MutableComponent {
        return if (isFake()) Component.translatable("block.cobblemon.gilded_chest") else super.getName()
    }

    fun isFake() = (type == Type.FAKE)

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!world.isClientSide) {
            if (isFake() && (player is ServerPlayer)) {
                spawnPokemon(world, pos, state, player)
            }
            val bEntity = world.getBlockEntity(pos) as? GildedChestBlockEntity
            bEntity?.setRemoved()
            val resultState =
                if (state.fluidState.`is`(Fluids.WATER)) Blocks.WATER.defaultBlockState() else Blocks.AIR.defaultBlockState()
            world.setBlockAndUpdate(pos, resultState)
            return resultState
        }
        return Blocks.AIR.defaultBlockState()
    }

    private fun spawnPokemon(world: Level, pos: BlockPos, state: BlockState, player: ServerPlayer): InteractionResult {
        val properties = "$POKEMON_ARGS lvl=${LEVEL_RANGE.random()}"
        val pokemon = PokemonProperties.parse(properties)
        val entity = pokemon.createEntity(world)

        // The yaw based on the block's facing direction
        val yaw = facingToYaw[state.getValue(HorizontalDirectionalBlock.FACING)] ?: 0.0F

        entity.entityData.set(PokemonEntity.SPAWN_DIRECTION, facingToYaw[state.getValue(HorizontalDirectionalBlock.FACING)])
        val offsetDir = state.getValue(HorizontalDirectionalBlock.FACING)
        val vec = pos.toVec3d().add(offsetDir.stepX * 0.1 + 0.5, 0.0, offsetDir.stepZ * 0.1 + 0.5)
        entity.moveTo(vec.x, vec.y, vec.z, yaw, entity.xRot)
        world.addFreshEntity(entity)

        world.removeBlock(pos, false)
        afterOnServer(ticks = 2) {
            if (player !in player.level().players()) {
                return@afterOnServer
            }
            val party = player.party()
            if (!player.isCreative) {
                entity.forceBattle(player)
            } else {
                world.playSound(null, pos, CobblemonSounds.GIMMIGHOUL_REVEAL, SoundSource.NEUTRAL)
            }
        }
        return InteractionResult.SUCCESS
    }

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (isFake()) {
            if (player is ServerPlayer) {
                return spawnPokemon(world, pos, state, player)
            } else {
                return InteractionResult.SUCCESS
            }
        }
        val entity = world.getBlockEntity(pos) as? GildedChestBlockEntity ?: return InteractionResult.FAIL
        if (world.getBlockState(pos.above()).isSolidRender(world, pos.above())) return InteractionResult.FAIL
        player.openMenu(entity)
        if (!player.level().isClientSide) {
            PiglinAi.angerNearbyPiglins(player, true)
        }
        return InteractionResult.SUCCESS
    }

    override fun onRemove(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!state.`is`(newState.block) && !world.isClientSide) {
            val chest = world.getBlockEntity(pos) as? GildedChestBlockEntity
            chest?.let {
                Containers.dropContents(world, pos, chest.inventoryContents)
            }
        }
    }

    override fun getRenderShape(state: BlockState) = RenderShape.ENTITYBLOCK_ANIMATED

    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState {
        return defaultBlockState()
            .setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.horizontalDirection.opposite)
            .setValue(WATERLOGGED, blockPlaceContext.level.getFluidState(blockPlaceContext.clickedPos).type == Fluids.WATER)
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(
            HORIZONTAL_FACING, rotation.rotate(
                state.getValue(HORIZONTAL_FACING) as Direction
            )
        ) as BlockState
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING) as Direction))
    }

    enum class Type(val poserId: ResourceLocation) : StringRepresentable {
        RED(cobblemonResource("gilded_chest")),
        BLUE(cobblemonResource("blue_gilded_chest")),
        GREEN(cobblemonResource("green_gilded_chest")),
        PINK(cobblemonResource("pink_gilded_chest")),
        WHITE(cobblemonResource("white_gilded_chest")),
        BLACK(cobblemonResource("black_gilded_chest")),
        YELLOW(cobblemonResource("yellow_gilded_chest")),
        FAKE(cobblemonResource("gilded_chest"));

        override fun getSerializedName(): String = name.lowercase()

        companion object {
            val CODEC = StringRepresentable.fromValues(::values)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean {
        return false
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun setPlacedBy(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        //Theoretically this is no longer needed according to https://fabricmc.net/2024/04/19/1205.html
        /*
        val blockEntity = world.getBlockEntity(pos)
        if (itemStack.get(DataComponentTypes.CUSTOM_NAME) !=  && blockEntity is GildedChestBlockEntity) {
            blockEntity.customName = itemStack.name
        }
        */
    }

    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        val blockEntity = world.getBlockEntity(pos) as? GildedChestBlockEntity ?: return
        blockEntity.tick()
    }

}