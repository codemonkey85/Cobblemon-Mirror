/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.controllers

import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.blockPositionsAsListRounded
import com.cobblemon.mod.common.util.cobblemonResource
import kotlin.math.max
import kotlin.math.min
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class GenericLiquidController : RideController {
    companion object {
        val KEY: ResourceLocation = cobblemonResource("swim/generic")
    }

    var speed = 1F
        private set
    var acceleration = 1F
        private set

    override val key: ResourceLocation = KEY
    override val poseProvider: PoseProvider = PoseProvider(PoseType.FLOAT)
        .with(PoseOption(PoseType.SWIM) { it.isSwimming && it.entityData.get(PokemonEntity.MOVING) })
    override val condition: (PokemonEntity) -> Boolean = { entity ->
        //This could be kinda weird... what if the top of the mon is in a fluid but the bottom isnt?
        Shapes.create(entity.boundingBox).blockPositionsAsListRounded().any {
            if (entity.isInWater || entity.isUnderWater) {
                return@any true
            }
            val blockState = entity.level().getBlockState(it)
            return@any !blockState.fluidState.isEmpty
        }
    }

    override fun speed(entity: PokemonEntity, driver: Player): Float {
        return min(max(this.speed + this.acceleration(), 0.0F), 1.0F)
    }

    private fun acceleration(): Float {
        return (1 / ((300 * this.speed) + (18.5F - (this.acceleration * 5.3F)))) * (0.9F * ((this.acceleration + 1) / 2))
    }

    override fun rotation(entity: PokemonEntity, driver: LivingEntity): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(entity: PokemonEntity, driver: Player, input: Vec3): Vec3 {
        val f = driver.xxa * 0.1f
        var g = driver.zza * 0.3f
        if (g <= 0.0f) {
            g *= 0.12f
        }

        return Vec3(f.toDouble(), 0.0, g.toDouble())
    }

    override fun canJump(entity: PokemonEntity, driver: Player): Boolean {
        TODO("Not yet implemented")
    }

    override fun jumpForce(entity: PokemonEntity, driver: Player, jumpStrength: Int): Vec3 {
        TODO("Not yet implemented")
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeFloat(this.speed)
        buffer.writeFloat(this.acceleration)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        this.speed = buffer.readFloat()
        this.acceleration = buffer.readFloat()
    }
}