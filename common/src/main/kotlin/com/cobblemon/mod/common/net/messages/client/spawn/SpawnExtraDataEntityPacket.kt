/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.mixin.invoker.ClientPlayNetworkHandlerInvoker
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.PacketUtils
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.phys.Vec3

abstract class SpawnExtraDataEntityPacket<T: NetworkPacket<T>, E : Entity>(private val vanillaSpawnPacket: ClientboundAddEntityPacket) : NetworkPacket<T> {
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        this.encodeEntityData(buffer)
        this.vanillaSpawnPacket.write(buffer)
    }

    abstract fun encodeEntityData(buffer: RegistryFriendlyByteBuf)

    abstract fun applyData(entity: E)

    abstract fun checkType(entity: Entity): Boolean

    fun spawnAndApply(client: Minecraft) {
        client.execute {
            val player = client.player ?: return@execute
            val world = player.level() as? ClientLevel ?: return@execute
            // This is a copy pasta of ClientPlayNetworkHandler#onEntitySpawn
            // This exists due to us needing to do everything it does except spawn the entity in the world.
            // We invoke applyData then we add the entity to the world.
            PacketUtils.ensureRunningOnSameThread(this.vanillaSpawnPacket, player.connection, client)
            val entityType = this.vanillaSpawnPacket.type
            val entity = entityType.create(world) ?: return@execute
            entity.recreateFromPacket(this.vanillaSpawnPacket)
            entity.deltaMovement = Vec3(
                this.vanillaSpawnPacket.xa,
                this.vanillaSpawnPacket.ya,
                this.vanillaSpawnPacket.za
            )
            // Cobblemon start
            if (this.checkType(entity)) {
                this.applyData(entity as E)
            }
            // Cobblemon end
            world.addEntity(entity)
            (player.connection as ClientPlayNetworkHandlerInvoker).callPlaySpawnSound(entity)
        }
    }

    companion object {
        fun decodeVanillaPacket(buffer: RegistryFriendlyByteBuf) = ClientboundAddEntityPacket(buffer)
    }
}