/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import net.minecraft.world.entity.player.Player
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

data class RidingManager(val entity: PokemonEntity) {
    var lastSpeed = 0F

    /**
     * Responsible for handling riding conditions and transitions amongst controllers. This will tick
     * whenever the entity receives a tickControlled interaction.
     */
    fun tick(entity: PokemonEntity, driver: Player, input: Vec3) {
        val controller = entity.pokemon.riding.controllers.firstOrNull { it.condition.invoke(entity) } ?: return

        val poser = controller.poseProvider
        entity.entityData.set(PokemonEntity.POSE_TYPE, poser.select(entity))

        driver.displayClientMessage(Component.literal("Speed: ").withStyle { it.withColor(ChatFormatting.GREEN) }.append(Component.literal("$lastSpeed b/t")), true)
    }

    fun speed(entity: PokemonEntity, driver: Player): Float {
        val controller = entity.pokemon.riding.controllers.firstOrNull { it.condition.invoke(entity) }
        this.lastSpeed = controller?.speed(entity, driver) ?: 0.05F
        return this.lastSpeed
    }

    fun controlledRotation(entity: PokemonEntity, driver: Player): Vec2 {
        val controller = entity.pokemon.riding.controllers.firstOrNull { it.condition.invoke(entity) }
        return controller?.rotation(driver) ?: Vec2.ZERO
    }

    fun velocity(entity: PokemonEntity, driver: Player, input: Vec3): Vec3 {
        val controller = entity.pokemon.riding.controllers.firstOrNull { it.condition.invoke(entity) }
        return controller?.velocity(driver, input) ?: Vec3.ZERO
    }

    fun canJump(entity: PokemonEntity, driver: Player): Boolean {
        val controller = entity.pokemon.riding.controllers.firstOrNull { it.condition.invoke(entity) }
        return controller?.canJump(entity, driver) ?: false
    }

    fun jumpVelocity(entity: PokemonEntity, driver: Player, jumpStrength: Int): Vec3 {
        val controller = entity.pokemon.riding.controllers.firstOrNull { it.condition.invoke(entity) }
        return controller?.jumpForce(entity, driver, jumpStrength) ?: Vec3.ZERO
    }
}