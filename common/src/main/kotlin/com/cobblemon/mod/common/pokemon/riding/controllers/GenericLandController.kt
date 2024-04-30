/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.controllers

import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.blockPositionsAsListRounded
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonElement
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import kotlin.math.max
import kotlin.math.min

class GenericLandController : RideController {
    companion object {
        val KEY: Identifier = cobblemonResource("land/generic")
    }

    var speed = 1F
        private set
    var acceleration = 1F
        private set

    override val key: Identifier = KEY
    override val poseProvider: PoseProvider = PoseProvider(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { it.dataTracker.get(PokemonEntity.MOVING) })
    override val condition: (PokemonEntity) -> Boolean = { entity ->
        //Are there any blocks under the mon that aren't air or fluid
        //Cant just check one block since some mons may be more than one block big
        //This should be changed so that the any predicate is only ran on blocks under the mon
        VoxelShapes.cuboid(entity.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (entity.isTouchingWater || entity.isSubmergedInWater) {
                return@any false
            }
            //This might not actually work, depending on what the yPos actually is. yPos of the middle of the entity? the feet?
            if (it.y.toDouble() == (entity.pos.y)) {
                val blockState = entity.world.getBlockState(it.down())
                return@any !blockState.isAir && blockState.fluidState.isEmpty
            }
            true
        }
    }

    override fun speed(entity: PokemonEntity, driver: PlayerEntity): Float {
        return min(max(this.speed + this.acceleration(), 0.0F), 1.0F)
    }

    private fun acceleration(): Float {
        return (1 / ((300 * this.speed) + (18.5F - (this.acceleration * 5.3F)))) * (0.9F * ((this.acceleration + 1) / 2))
    }

    override fun rotation(driver: LivingEntity): Vec2f {
        return Vec2f(driver.pitch * 0.5f, driver.yaw)
    }

    override fun velocity(driver: PlayerEntity, input: Vec3d): Vec3d {
        val f = driver.sidewaysSpeed * 0.2f
        var g = driver.forwardSpeed * 0.5f
        if (g <= 0.0f) {
            g *= 0.25f
        }

        return Vec3d(f.toDouble(), 0.0, g.toDouble())
    }

    override fun encode(buffer: PacketByteBuf) {
        super.encode(buffer)
        buffer.writeFloat(this.speed)
        buffer.writeFloat(this.acceleration)
    }

    override fun decode(buffer: PacketByteBuf) {
        this.speed = buffer.readFloat()
        this.acceleration = buffer.readFloat()
    }
}