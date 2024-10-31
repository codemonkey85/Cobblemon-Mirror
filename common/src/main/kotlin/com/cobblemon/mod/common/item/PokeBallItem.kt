/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.isServerSide
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.level.Level
import kotlin.math.cos

class PokeBallItem(
        val pokeBall: PokeBall
) : CobblemonItem(Properties().apply {
    if (pokeBall.name == PokeBalls.MASTER_BALL.name) {
        // Master balls are a netherite product and should be fire immune
        fireResistant()
    }
}) {

    override fun use(world: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        if (world.isServerSide()) {
            throwPokeBall(world, player as ServerPlayer)
        }
        if (!player.abilities.instabuild) {
            itemStack.shrink(1)
        }
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide)
    }

    private fun throwPokeBall(world: Level, player: ServerPlayer) {
        val pokeBallEntity = EmptyPokeBallEntity(pokeBall, player.level(), player).apply {
            val overhandFactor: Float = if (player.xRot < 0) {
                5f * cos(player.xRot.toRadians())
            } else {
                5f
            }

            shootFromRotation(player, player.xRot - overhandFactor, player.yRot, 0.0f, pokeBall.throwPower, 1.0f)
            setPos(position().add(deltaMovement.normalize().scale(1.0)))
            owner = player
        }
        world.addFreshEntity(pokeBallEntity)
    }
}