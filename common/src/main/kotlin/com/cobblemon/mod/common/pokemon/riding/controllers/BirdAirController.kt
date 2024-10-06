/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.controllers

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.withQueryValue
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class BirdAirController : RideController {
    override val key = KEY
    override val poseProvider = PoseProvider(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { it.deltaMovement.horizontalDistance() > 0.1 })
    override val condition: (PokemonEntity) -> Boolean = { true }

    var gravity: Expression = "0".asExpression()
        private set
    var horizontalAcceleration: Expression = "0.1".asExpression()
        private set
    var verticalAcceleration: Expression = "0.1".asExpression()
        private set
    var speed: Expression = "1.0".asExpression()
        private set

    override fun speed(entity: PokemonEntity, driver: Player): Float {
        return getRuntime(entity).resolveFloat(speed)
    }

    override fun rotation(entity: PokemonEntity, driver: LivingEntity): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(entity: PokemonEntity, driver: Player, input: Vec3): Vec3 {
        val f = driver.xxa * 0.2f
        var g = driver.zza
        if (g <= 0.0f) {
            g *= 0.25f
        }

        val velocity = Vec3(f.toDouble(), 0.0, g.toDouble())

        return velocity
    }

    override fun canJump(
        entity: PokemonEntity,
        driver: Player
    ): Boolean {
        return true
    }

    override fun jumpForce(
        entity: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3(0.0, 1.0, 0.0).multiply(0.0, jumpStrength.toDouble() / 5, 0.0)
    }

    override fun gravity(entity: PokemonEntity, regularGravity: Double): Double? {
        return getRuntime(entity)
            .withQueryValue("gravity", DoubleValue(regularGravity))
            .resolveFloat(gravity)
            .toDouble()
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeString(gravity.toString())
        buffer.writeString(horizontalAcceleration.toString())
        buffer.writeString(verticalAcceleration.toString())
        buffer.writeString(speed.toString())
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        gravity = buffer.readString().asExpression()
        horizontalAcceleration = buffer.readString().asExpression()
        verticalAcceleration = buffer.readString().asExpression()
        speed = buffer.readString().asExpression()
    }


    companion object {
        val KEY = cobblemonResource("air/bird")
    }
}