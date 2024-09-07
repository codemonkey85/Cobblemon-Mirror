/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.block.TumblestoneBlock
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

class PlantTumblestoneContext(val pos: BlockPos, val block: TumblestoneBlock)

class PlantTumblestoneCriterion(
    playerCtx: Optional<ContextAwarePredicate>
): SimpleCriterionCondition<PlantTumblestoneContext>(playerCtx) {

    companion object {
        val CODEC: Codec<PlantTumblestoneCriterion> = RecordCodecBuilder.create { it.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(PlantTumblestoneCriterion::playerCtx)
        ).apply(it, ::PlantTumblestoneCriterion) }
    }

    override fun matches(player: ServerPlayer, context: PlantTumblestoneContext): Boolean {
        return context.block.canGrow(context.pos, player.level())
    }
}

///**
// * A criterion condition for when a tumblestone block is planted
// * @param id The identifier of the criterion
// * @param predicate The predicate for the criterion
// *
// * @author Aethen
// * @since 02/28/2024
// */
//class PlantTumblestoneCriterionCondition(id: Identifier, predicate: LootContextPredicate) :
//    SimpleCriterionCondition<PlantTumblestoneContext>(id, predicate) {
//    override fun toJson(json: JsonObject) {
//        // Add properties to json if needed. None needed for this criterion
//    }
//
//    override fun fromJson(json: JsonObject) {
//        // Parse properties from json if needed. None needed for this criterion
//    }
//
//    /**
//     * Checks if the tumblestone block can grow at the given position
//     * @param player The player that planted the tumblestone block
//     * @param context The context of the criterion
//     * @return True if the tumblestone block can grow at the given position, false otherwise
//     */
//    override fun matches(player: ServerPlayer, context: PlantTumblestoneContext): Boolean {
//        return context.tumbleStoneBlock.canGrow(context.pos, player.level())
//    }
//}
//
///**
// * The context of the [PlantTumblestoneCriterionCondition]
// * @param pos The position of the tumblestone block
// * @param tumbleStoneBlock The tumblestone block
// */
//open class PlantTumblestoneContext(var pos: BlockPos, var tumbleStoneBlock: TumblestoneBlock)