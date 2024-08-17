/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.entity.boat.CobblemonBoatEntity
import com.cobblemon.mod.common.entity.boat.CobblemonBoatType
import com.cobblemon.mod.common.entity.boat.CobblemonChestBoatEntity
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.HitResult

class CobblemonBoatItem(val boatType: CobblemonBoatType, val hasChest: Boolean, settings: Properties) : CobblemonItem(settings) {

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = user.getItemInHand(hand)
        val hitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY)
        if (hitResult.type == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack)
        }
        val vec3d = user.getViewVector(1F)
        val eyePos = user.eyePosition
        world.getEntities(user, user.boundingBox.expandTowards(vec3d.scale(5.0)).inflate(1.0), RIDERS).forEach { entity ->
            val box = entity.boundingBox.inflate(entity.pickRadius.toDouble())
            if (box.contains(eyePos)) {
                return InteractionResultHolder.pass(stack)
            }
        }
        if (hitResult.type != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack)
        }
        val boatEntity = this.createBoat(world, hitResult)
        boatEntity.boatType = this.boatType
        boatEntity.yRot = user.yRot
        if (!world.noCollision(boatEntity, boatEntity.boundingBox)) {
            return InteractionResultHolder.fail(stack)
        }
        if (!world.isClientSide) {
            world.addFreshEntity(boatEntity)
            world.gameEvent(user, GameEvent.ENTITY_PLACE, hitResult.blockPos)
            if (!user.abilities.instabuild) {
                stack.shrink(1)
            }
        }
        user.awardStat(Stats.ITEM_USED.get(this))
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide)
    }

    private fun createBoat(world: Level, hitResult: HitResult): CobblemonBoatEntity {
        if (this.hasChest) {
            return CobblemonChestBoatEntity(world, hitResult.location.x, hitResult.location.y, hitResult.location.z)
        }
        return CobblemonBoatEntity(world, hitResult.location.x, hitResult.location.y, hitResult.location.z)
    }

    companion object {

        private val RIDERS = EntitySelector.NO_SPECTATORS.and(Entity::isPickable)

    }

}