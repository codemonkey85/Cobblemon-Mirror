/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.net

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.PacketRegisterInfo
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer

/**
 * Good news, everyone! I've figured out how to remove a bunch of blank boilerplate from the network manager!
 * Now some of you may die from cringe, but that is a sacrifice I am willing to make.
 *
 * It basically needs this wrapping to execute T-specific and Fabric-specific stuff. Neo and Fabric do
 * very different things, we can do separable parts here which we can't in NeoForge.
 *
 * @author Hiroku
 * @since June 8th, 2024
 */
class FabricPacketInfo<T : NetworkPacket<T>>(val info: PacketRegisterInfo<T>) {
    fun registerPacket(client: Boolean) {
        (if (client) {
            PayloadTypeRegistry.playS2C()
        } else {
            PayloadTypeRegistry.playC2S()
        })
        .register(info.payloadId, info.codec)
    }

    fun registerClientHandler() {
        ClientPlayNetworking.registerGlobalReceiver(info.payloadId) { obj, _ ->
            val handler = info.handler as ClientNetworkPacketHandler<T>
            handler.handle(obj, Minecraft.getInstance())
        }
    }

    fun registerServerHandler() {
        ServerPlayNetworking.registerGlobalReceiver(info.payloadId) { obj, context ->
            val handler = info.handler as ServerNetworkPacketHandler<T>
            handler.handle(obj, context.player().server!!, context.player() as ServerPlayer)
        }
    }
}