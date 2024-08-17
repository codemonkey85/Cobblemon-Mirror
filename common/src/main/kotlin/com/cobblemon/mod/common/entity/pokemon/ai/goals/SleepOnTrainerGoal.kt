/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.BedBlock
import net.minecraft.world.phys.AABB

/**
 * AI goal for sleeping on top of a player when they hop into a bed, like cats.
 *
 * @author Hiroku
 * @since July 18th, 2022
 */
class SleepOnTrainerGoal(private val pokemonEntity: PokemonEntity) : Goal() {
    private var owner: Player? = null
    private var bedPos: BlockPos? = null
    private var ticksOnBed = 0

    override fun canUse(): Boolean {
        if (!pokemonEntity.pokemon.isPlayerOwned() || !pokemonEntity.behaviour.resting.willSleepOnBed || pokemonEntity.pokemon.status != null) {
            return false
        }
        val livingEntity = pokemonEntity.owner
        if (livingEntity is Player) {
            owner = livingEntity
            if (!livingEntity.isSleeping()) {
                return false
            }
            if (pokemonEntity.distanceToSqr(owner) > 100.0) {
                return false
            }
            val blockPos = owner!!.blockPosition()
            val blockState = pokemonEntity.level().getBlockState(blockPos)
            if (blockState.`is`(BlockTags.BEDS)) {
                bedPos = blockState.getOptionalValue(BedBlock.FACING).orElse(null)
                    ?.let { direction -> blockPos.relative(direction.opposite) }
                    ?: BlockPos(blockPos)
                return !cannotSleep()
            }
        }

        return false
    }

    private fun cannotSleep(): Boolean {
        val closePokemon = pokemonEntity.level().getEntitiesOfClass(PokemonEntity::class.java, AABB(bedPos).inflate(2.0))
        return closePokemon.any { it.pokemon.status?.status == Statuses.SLEEP && it != pokemonEntity }
    }

    override fun canContinueToUse(): Boolean {
        val owner = owner
        return owner is ServerPlayer && owner.isSleeping && bedPos != null && !cannotSleep()
    }

    override fun start() {
        if (bedPos != null) {
            pokemonEntity.navigation.moveTo(
                bedPos!!.x.toDouble(),
                bedPos!!.y.toDouble(),
                bedPos!!.z.toDouble(),
                0.7
            )
        }
    }

    override fun stop() {
        pokemonEntity.pokemon.status = null
//        val f = pokemonEntity.world.getSkyAngle(1.0f)
//        if (owner!!.sleepTimer >= 100 && f.toDouble() > 0.77 && f.toDouble() < 0.8 && pokemonEntity.world.getRandom().nextFloat() < 0.7) {
//            dropMorningGifts()
//        }
        ticksOnBed = 0
        pokemonEntity.navigation.stop()
    }

//    private fun dropMorningGifts() {
//        val random = cat.random
//        val mutable = BlockPos.Mutable()
//        mutable.set(cat.blockPos)
//        cat.teleport(
//            (mutable.x + random.nextInt(11) - 5).toDouble(),
//            (mutable.y + random.nextInt(5) - 2).toDouble(),
//            (mutable.z + random.nextInt(11) - 5).toDouble(),
//            false
//        )
//        mutable.set(cat.blockPos)
//        val lootTable = cat.world.server!!.lootManager.getTable(LootTables.CAT_MORNING_GIFT_GAMEPLAY)
//        val builder = LootContext.Builder(cat.world as ServerLevel)
//            .parameter(LootContextParameters.ORIGIN, cat.pos).parameter(
//                LootContextParameters.THIS_ENTITY,
//                cat
//            ).random(random)
//        val list = lootTable.generateLoot(builder.build(LootContextTypes.GIFT))
//        val var6: Iterator<*> = list.iterator()
//        while (var6.hasNext()) {
//            val itemStack = var6.next() as ItemStack
//            cat.world.spawnEntity(
//                ItemEntity(
//                    cat.world, mutable.x.toDouble() - Mth.sin(
//                        cat.bodyYaw * 0.017453292f
//                    ).toDouble(), mutable.y.toDouble(), mutable.z.toDouble() + Mth.cos(
//                        cat.bodyYaw * 0.017453292f
//                    ).toDouble(), itemStack
//                )
//            )
//        }
//    }

    override fun tick() {
        if (owner != null && bedPos != null) {
            if (pokemonEntity.distanceTo(owner) < 1.5) {
                ++ticksOnBed
                if (ticksOnBed > adjustedTickDelay(16)) {
                    pokemonEntity.pokemon.status = PersistentStatusContainer(Statuses.SLEEP)
                } else {
                    pokemonEntity.lookAt(owner, 45.0f, 45.0f)
                }
            } else {
                pokemonEntity.pokemon.status = null
            }
        }
    }
}
