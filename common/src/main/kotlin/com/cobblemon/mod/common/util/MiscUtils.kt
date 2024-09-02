/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.math.min
import kotlin.random.Random
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.phys.shapes.VoxelShape

fun cobblemonResource(path: String) = ResourceLocation.fromNamespaceAndPath(Cobblemon.MODID, path)
fun cobblemonModel(path: String, variant: String) =
    ModelResourceLocation(cobblemonResource(path), variant)

fun String.asTranslated() = Component.translatable(this)
fun String.asResource() = ResourceLocation.parse(this)
fun String.asTranslated(vararg data: Any) = Component.translatable(this, *data)
fun String.isInt() = this.toIntOrNull() != null
fun String.isHigherVersion(other: String): Boolean {
    val thisSplits = split(".")
    val thatSplits = other.split(".")

    val thisCount = thisSplits.size
    val thatCount = thatSplits.size

    val min = min(thisCount, thatCount)
    for (i in 0 until min) {
        val thisDigit = thisSplits[i].toIntOrNull()
        val thatDigit = thatSplits[i].toIntOrNull()
        if (thisDigit == null || thatDigit == null) {
            return false
        }

        if (thisDigit > thatDigit) {
            return true
        } else if (thisDigit < thatDigit) {
            return false
        }
    }

    return thisCount > thatCount
}

fun String.substitute(placeholder: String, value: Any?) = replace("{{$placeholder}}", value?.toString() ?: "")

val Pair<Boolean, Boolean>.either: Boolean get() = first || second

fun Random.nextBetween(min: Float, max: Float): Float {
    return nextFloat() * (max - min) + min;
}

fun Random.nextBetween(min: Double, max: Double): Double {
    return nextDouble() * (max - min) + min;
}

fun Random.nextBetween(min: Int, max: Int): Int {
    return nextInt(max - min + 1) + min
}

infix fun <A, B> A.toDF(b: B): com.mojang.datafixers.util.Pair<A, B> = com.mojang.datafixers.util.Pair(this, b)

fun isUuid(string: String) : Boolean {
    return Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$").matches(string)
}

fun VoxelShape.blockPositionsAsList(): List<BlockPos> {
    val result = mutableListOf<BlockPos>()
    forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
        for (x in minX.toInt() until maxX.toInt()) {
            for (y in minY.toInt() until maxY.toInt()) {
                for (z in minZ.toInt() until maxZ.toInt()) {
                    result.add(BlockPos(x, y, z))
                }
            }
        }
    }

    return result
}

operator fun <T> Consumer<T>.plus(action: (T) -> Unit): Consumer<T> {
    return andThen(action)
}

fun chainFutures(others: Iterator<() -> CompletableFuture<*>>, finalFuture: CompletableFuture<Unit>) {
    if (!others.hasNext()) {
        finalFuture.complete(Unit)
        return
    }

    others.next().invoke().thenApply {
        chainFutures(others, finalFuture)
    }
}

val PosableState.isBattling: Boolean
    get() = (getEntity() as? PokemonEntity)?.isBattling == true || (getEntity() as? NPCEntity)?.isInBattle() == true
val PosableState.isUnderWater: Boolean
    get() = getEntity()?.isUnderWater == true
val PosableState.isInWater: Boolean
    get() = getEntity()?.isInWater == true
val PosableState.isInWaterOrRain: Boolean
    get() = getEntity()?.isInWaterOrRain == true

fun InteractionHand.toEquipmentSlot(): EquipmentSlot {
    return when (this) {
        InteractionHand.MAIN_HAND -> EquipmentSlot.MAINHAND
        InteractionHand.OFF_HAND -> EquipmentSlot.OFFHAND
    }
}

fun EquipmentSlot.toHand(): InteractionHand {
    return when (this) {
        EquipmentSlot.MAINHAND -> InteractionHand.MAIN_HAND
        EquipmentSlot.OFFHAND -> InteractionHand.OFF_HAND
        else -> throw IllegalArgumentException("Invalid equipment slot: $this")
    }
}