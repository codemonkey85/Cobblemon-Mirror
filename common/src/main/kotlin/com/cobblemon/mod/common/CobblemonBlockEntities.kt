/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.block.entity.PCBlockEntity
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import com.cobblemon.mod.common.block.entity.FossilAnalyzerBlockEntity
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.entity.RestorationTankBlockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockBuilder
import com.cobblemon.mod.common.block.entity.*
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object CobblemonBlockEntities : PlatformRegistry<Registry<BlockEntityType<*>>, RegistryKey<Registry<BlockEntityType<*>>>, BlockEntityType<*>>() {

    override val registry: Registry<BlockEntityType<*>> = Registries.BLOCK_ENTITY_TYPE
    override val registryKey: RegistryKey<Registry<BlockEntityType<*>>> = RegistryKeys.BLOCK_ENTITY_TYPE

    @JvmField
    val HEALING_MACHINE: BlockEntityType<HealingMachineBlockEntity> = this.create("healing_machine", BlockEntityType.Builder.create(::HealingMachineBlockEntity, CobblemonBlocks.HEALING_MACHINE).build(null))

    @JvmField
    val PC: BlockEntityType<PCBlockEntity> = this.create("pc", BlockEntityType.Builder.create(::PCBlockEntity, CobblemonBlocks.PC).build(null))

    @JvmField
    val BERRY = this.create("berry", BlockEntityType.Builder.create(::BerryBlockEntity, *CobblemonBlocks.berries().values.toTypedArray()).build(null))

    @JvmField
    val PASTURE: BlockEntityType<PokemonPastureBlockEntity> = this.create("pasture", BlockEntityType.Builder.create(::PokemonPastureBlockEntity, CobblemonBlocks.PASTURE).build(null))
    @JvmField
    val SIGN: BlockEntityType<CobblemonSignBlockEntity> = this.create("sign", BlockEntityType.Builder.create(::CobblemonSignBlockEntity, CobblemonBlocks.APRICORN_SIGN, CobblemonBlocks.APRICORN_WALL_SIGN).build(null))
    @JvmField
    val HANGING_SIGN: BlockEntityType<CobblemonHangingSignBlockEntity> = this.create("hanging_sign", BlockEntityType.Builder.create(::CobblemonHangingSignBlockEntity, CobblemonBlocks.APRICORN_HANGING_SIGN, CobblemonBlocks.APRICORN_WALL_HANGING_SIGN).build(null))

    @JvmField
    val FOSSIL_MULTIBLOCK: BlockEntityType<FossilMultiblockEntity> = this.create("fossil_multiblock",
        BlockEntityType.Builder.create({ pos, state -> FossilMultiblockEntity(pos, state, FossilMultiblockBuilder(pos)) },
            CobblemonBlocks.MONITOR
        ).build(null)
    )

    @JvmField
    val RESTORATION_TANK: BlockEntityType<RestorationTankBlockEntity> = this.create("restoration_tank",
        BlockEntityType.Builder.create({ pos, state -> RestorationTankBlockEntity(pos, state, FossilMultiblockBuilder(pos)) },
            CobblemonBlocks.RESTORATION_TANK
        ).build(null)
    )

    @JvmField
    val FOSSIL_ANALYZER: BlockEntityType<FossilAnalyzerBlockEntity> = this.create("fossil_analyzer",
        BlockEntityType.Builder.create({ pos, state -> FossilAnalyzerBlockEntity(pos, state, FossilMultiblockBuilder(pos)) },
            CobblemonBlocks.FOSSIL_ANALYZER
        ).build(null)
    )

    val NEST: BlockEntityType<NestBlockEntity> = this.create("nest",
        BlockEntityType.Builder.create(
            ::NestBlockEntity,
            CobblemonBlocks.NETHER_NEST,
            CobblemonBlocks.WATER_NEST,
            CobblemonBlocks.CAVE_NEST,
            CobblemonBlocks.BASE_NEST
        ).build(null))

    val EGG: BlockEntityType<EggBlockEntity> = this.create("egg",
            BlockEntityType.Builder.create(
                ::EggBlockEntity,
                CobblemonBlocks.EGG
            ).build(null)
        )
}
