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
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class SwimDashController : RideController {
    companion object {
        val KEY: ResourceLocation = cobblemonResource("swim/dash")
        const val DASH_TICKS: Int = 60
    }

    var dashSpeed = 1F
        private set
    override val key: ResourceLocation = KEY
    override val poseProvider: PoseProvider = PoseProvider(PoseType.FLOAT).with(PoseOption(PoseType.SWIM) { it.isSwimming && it.entityData.get(PokemonEntity.MOVING) })
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

    /** Indicates that we are currently enacting a dash, and that further movement inputs should be ignored */
    private var dashing = false
    private var ticks = 0

    override fun speed(entity: PokemonEntity, driver: Player): Float {
        if(this.dashing) {
            if(this.ticks++ >= DASH_TICKS) {
                this.dashing = false
            }

            return 0.0F
        }

        this.dashing = true
        return this.dashSpeed
    }

    override fun rotation(entity: PokemonEntity, driver: LivingEntity): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(entity: PokemonEntity, driver: Player, input: Vec3): Vec3 {
        val f = driver.xxa * 0.05f
        var g = driver.zza * 0.6f
        if (g <= 0.0f) {
            g *= 0.25f
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
        buffer.writeFloat(this.dashSpeed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        this.dashSpeed = buffer.readFloat()
    }
}
