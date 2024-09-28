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
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.isInBattle
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("DEPRECATED", "OVERRIDE_DEPRECATION")
class HealingMachineBlock(settings: Properties) : BaseEntityBlock(settings) {
    companion object {
        val CODEC: MapCodec<HealingMachineBlock> = simpleCodec(::HealingMachineBlock)

        private val NORTH_SOUTH_AABB = Shapes.or(
            Shapes.box(0.0, 0.0, 0.0, 1.0, 0.625, 1.0),
            Shapes.box(0.0625, 0.625, 0.0, 0.9375, 0.875, 0.125),
            Shapes.box(0.0625, 0.625, 0.875, 0.9375, 0.875, 1.0),
            Shapes.box(0.0625, 0.625, 0.125, 0.1875, 0.75, 0.875),
            Shapes.box(0.8125, 0.625, 0.125, 0.9375, 0.75, 0.875),
            Shapes.box(0.1875, 0.625, 0.125, 0.8125, 0.6875, 0.875)
        )

        private val WEST_EAST_AABB = Shapes.or(
            Shapes.box(0.0, 0.0, 0.0, 1.0, 0.625, 1.0),
            Shapes.box(0.875, 0.625, 0.0625, 1.0, 0.875, 0.9375),
            Shapes.box(0.0, 0.625, 0.0625, 0.125, 0.875, 0.9375),
            Shapes.box(0.125, 0.625, 0.0625, 0.875, 0.75, 0.1875),
            Shapes.box(0.125, 0.625, 0.8125, 0.875, 0.75, 0.9375),
            Shapes.box(0.125, 0.625, 0.1875, 0.875, 0.6875, 0.8125)
        )

        // Charge level 6 is used only when healing machine is active
        const val MAX_CHARGE_LEVEL = 5
        val CHARGE_LEVEL: IntegerProperty = IntegerProperty.create("charge", 0, MAX_CHARGE_LEVEL + 1)
        val NATURAL: BooleanProperty = BooleanProperty.create("natural")
    }

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(NATURAL, false)
            .setValue(CHARGE_LEVEL, 0))
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos, collisionContext: CollisionContext): VoxelShape {
        return when (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
            Direction.WEST -> WEST_EAST_AABB
            Direction.EAST -> WEST_EAST_AABB
            else -> NORTH_SOUTH_AABB
        }
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return HealingMachineBlockEntity(blockPos, blockState)
    }

    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.horizontalDirection)
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun isPathfindable(
        blockState: BlockState,
        pathComputationType: PathComputationType
    ): Boolean {
        return false
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HorizontalDirectionalBlock.FACING, NATURAL)
        builder.add(*arrayOf<Property<*>>(CHARGE_LEVEL))
    }

    override fun rotate(blockState: BlockState, rotation: Rotation): BlockState {
        return blockState.setValue(
            HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(
                HorizontalDirectionalBlock.FACING)))
    }

    override fun mirror(blockState: BlockState, mirror: Mirror): BlockState {
        return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)))
    }

    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block)) super.onRemove(state, world, pos, newState, moved)
    }

    override fun useWithoutItem(blockState: BlockState, world: Level, blockPos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS
        }

        val blockEntity = world.getBlockEntity(blockPos)
        if (blockEntity !is HealingMachineBlockEntity) {
            return InteractionResult.SUCCESS
        }

        val serverPlayerEntity = player as ServerPlayer

        if (blockEntity.isInUse) {
            player.sendSystemMessage(lang("healingmachine.alreadyinuse").red(), true)
            return InteractionResult.SUCCESS
        }

        if (serverPlayerEntity.isInBattle()) {
            player.sendSystemMessage(lang("healingmachine.inbattle").red(), true)
            return InteractionResult.SUCCESS
        }
        val party = serverPlayerEntity.party()
        if (party.none()) {
            player.sendSystemMessage(lang("healingmachine.nopokemon").red(), true)
            return InteractionResult.SUCCESS
        }

        if (party.none { pokemon -> pokemon.canBeHealed() }) {
            player.sendSystemMessage(lang("healingmachine.alreadyhealed").red(), true)
            return InteractionResult.SUCCESS
        }

        if (HealingMachineBlockEntity.isUsingHealer(player)) {
            player.sendSystemMessage(lang("healingmachine.alreadyhealing").red(), true)
            return InteractionResult.SUCCESS
        }

        if (blockEntity.canHeal(party)) {
            blockEntity.activate(player.uuid, party)
            player.sendSystemMessage(lang("healingmachine.healing").green(), true)
        } else {
            val neededCharge = player.party().getHealingRemainderPercent() - blockEntity.healingCharge
            player.sendSystemMessage(lang("healingmachine.notenoughcharge", "${((neededCharge/party.count())*100f).toInt()}%").red(), true)
        }
        party.forEach { it.tryRecallWithAnimation() }
        return InteractionResult.CONSUME
    }

    override fun setPlacedBy(world: Level, blockPos: BlockPos, blockState: BlockState, livingEntity: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(world, blockPos, blockState, livingEntity, itemStack)

        if (!world.isClientSide && livingEntity is ServerPlayer && livingEntity.isCreative) {
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity !is HealingMachineBlockEntity) {
                return
            }
            blockEntity.infinite = true
        }
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity !is HealingMachineBlockEntity) return

        if (random.nextInt(2) == 0 && blockEntity.healTimeLeft > 0) {
            val posX = pos.x.toDouble() + 0.5 + ((random.nextFloat() * 0.3F) * (if (random.nextInt(2) > 0) 1 else (-1))).toDouble()
            val posY = pos.y.toDouble() + 0.9
            val posZ = pos.z.toDouble() + 0.5 + ((random.nextFloat() * 0.3F) * (if (random.nextInt(2) > 0) 1 else (-1))).toDouble()
            world.addParticle(ParticleTypes.HAPPY_VILLAGER, posX, posY, posZ, 0.0, 0.0, 0.0)
        }
    }

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int = (world.getBlockEntity(pos) as? HealingMachineBlockEntity)?.currentSignal ?: 0

    override fun <T : BlockEntity> getTicker(world: Level, blockState: BlockState, blockWithEntityType: BlockEntityType<T>): BlockEntityTicker<T>? = createTickerHelper(blockWithEntityType, CobblemonBlockEntities.HEALING_MACHINE, HealingMachineBlockEntity.TICKER::tick)

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltip: MutableList<Component>,
        options: TooltipFlag
    ) {
        tooltip.add("block.${Cobblemon.MODID}.healing_machine.tooltip1".asTranslated().gray())
        tooltip.add("block.${Cobblemon.MODID}.healing_machine.tooltip2".asTranslated().gray())
    }
}