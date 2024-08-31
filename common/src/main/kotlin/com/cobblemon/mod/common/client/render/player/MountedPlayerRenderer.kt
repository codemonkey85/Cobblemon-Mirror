/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.player

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * @author landonjw
 */
object MountedPlayerRenderer {
    fun render(player: AbstractClientPlayer, entity: PokemonEntity, stack: PoseStack) {
        val camera = Minecraft.getInstance().gameRenderer.mainCamera
        val yaw = camera.yRot.toRadians()
        val isEntityFlying = false

        val offset = getOffset(player, entity, isEntityFlying, yaw)

        val matrix = stack.last().pose()
        if (isEntityFlying) {
            val cameraRotation = camera.rotation().get(Matrix4f())
            matrix.mul(cameraRotation)
        }
        matrix.translate(offset)
        if (isEntityFlying) matrix.rotateY(yaw)

        player.yBodyRot = player.yHeadRot
    }

    private fun getOffset(
        player: AbstractClientPlayer,
        entity: PokemonEntity,
        isEntityFlying: Boolean,
        yaw: Float
    ): Vector3f {
        val seats = entity.seats
        val seatIndex = entity.passengers.indexOf(player).takeIf { it != -1 && it < seats.size } ?: return player.position().toVector3f()
        val mountOrigin = (entity.delegate as PokemonClientDelegate).locatorStates["seat${seatIndex+1}"]?.getOrigin() ?: return Vector3f()
        val playerOrigin = Vec3(player.x, player.y, player.z)
        val offsetOrigin = mountOrigin.subtract(playerOrigin).subtract(0.0, 0.6, 0.0)

        val offsetMatrix = Matrix4f()
        if (isEntityFlying) offsetMatrix.rotateY(yaw)
        offsetMatrix.translate(offsetOrigin.toVector3f())
        return offsetMatrix.getTranslation(Vector3f())
    }
}