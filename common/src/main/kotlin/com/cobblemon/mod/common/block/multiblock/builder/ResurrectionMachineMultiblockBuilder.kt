package com.cobblemon.mod.common.block.multiblock.builder

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.multiblock.condition.BlockRelativeCondition
import com.cobblemon.mod.common.block.multiblock.condition.OrCondition
import net.minecraft.predicate.BlockPredicate
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

class ResurrectionMachineMultiblockBuilder(val centerPos: BlockPos) : MultiblockStructureBuilder {
    override val boundingBox: Box = Box.of(centerPos.toCenterPos(), 2.0, 2.0, 2.0)
    override val conditions = listOf(
        BlockRelativeCondition(
            BlockPredicate.Builder.create().blocks(CobblemonBlocks.FOSSIL_COMPARTMENT).build(),
            BlockPredicate.Builder.create().blocks(CobblemonBlocks.FOSSIL_MONITOR).build(),
            arrayOf(Direction.UP)
        ),
        OrCondition(
            BlockRelativeCondition(
                BlockPredicate.Builder.create().blocks(CobblemonBlocks.FOSSIL_COMPARTMENT).build(),
                BlockPredicate.Builder.create().blocks(CobblemonBlocks.FOSSIL_TUBE).build(),
                arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
            ),
            BlockRelativeCondition(
                BlockPredicate.Builder.create().blocks(CobblemonBlocks.FOSSIL_MONITOR).build(),
                BlockPredicate.Builder.create().blocks(CobblemonBlocks.FOSSIL_TUBE).build(),
                arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
            )
        )
    )

    override fun form() {
        println("Resurrection machine formed")
    }


}
