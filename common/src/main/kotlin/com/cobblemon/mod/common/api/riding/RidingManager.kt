/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.riding.controller.RideController
import net.minecraft.world.entity.player.Player
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

data class RidingManager(val entity: PokemonEntity) {
    var lastSpeed = 0F
    var states: MutableMap<ResourceLocation, RidingState> = mutableMapOf()

    val runtime: MoLangRuntime by lazy {
        MoLangRuntime()
            .setup()
            .withQueryValue("entity", entity.struct)
            .also {
                it.environment.query.addFunction("passenger_count") { DoubleValue(entity.passengers.size.toDouble()) }
            }
    }

    fun <T : RidingState> getState(id: ResourceLocation, constructor: (PokemonEntity) -> T): T {
        val storedState = states[id]
        if (storedState == null) {
            val newState = constructor(entity)
            states[id] = newState
            return newState
        }

        return storedState as T
    }

    fun getController(entity: PokemonEntity): RideController? {
        return entity.pokemon.riding.controller?.takeIf { it.condition.invoke(entity) }
    }

    /**
     * Responsible for handling riding conditions and transitions amongst controllers. This will tick
     * whenever the entity receives a tickControlled interaction.
     */
    fun tick(entity: PokemonEntity, driver: Player, input: Vec3) {
        val controller = getController(entity) ?: return

        val pose = controller.pose(entity)
        entity.entityData.set(PokemonEntity.POSE_TYPE, pose)

        driver.displayClientMessage(Component.literal("Speed: ").withStyle { it.withColor(ChatFormatting.GREEN) }.append(Component.literal("$lastSpeed b/t")), true)
    }

    fun speed(entity: PokemonEntity, driver: Player): Float {
        val controller = getController(entity) ?: return 0.05F
        this.lastSpeed = controller.speed(entity, driver)
        return this.lastSpeed
    }

    fun controlledRotation(entity: PokemonEntity, driver: Player): Vec2 {
        val controller = getController(entity) ?: return Vec2.ZERO
        return controller.rotation(entity, driver)
    }

    fun velocity(entity: PokemonEntity, driver: Player, input: Vec3): Vec3 {
        val controller = getController(entity) ?: return Vec3.ZERO
        return controller.velocity(entity, driver, input)
    }

    fun canJump(entity: PokemonEntity, driver: Player): Boolean {
        val controller = getController(entity) ?: return false
        return controller.canJump(entity, driver)
    }

    fun jumpVelocity(entity: PokemonEntity, driver: Player, jumpStrength: Int): Vec3 {
        val controller = getController(entity) ?: return Vec3.ZERO
        return controller.jumpForce(entity, driver, jumpStrength)
    }

    fun gravity(entity: PokemonEntity, regularGravity: Double): Double? {
        val controller = getController(entity) ?: return null
        return controller.gravity(entity, regularGravity)
    }
}