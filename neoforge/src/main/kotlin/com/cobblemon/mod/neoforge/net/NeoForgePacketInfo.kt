/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.net

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.PacketRegisterInfo
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar

/**
 * Good news, everyone! I've figured out how to remove a bunch of blank boilerplate from the network manager!
 * Now some of you may die from cringe, but that is a sacrifice I am willing to make.
 *
 * It basically needs this wrapping to execute T-specific and NeoForge-specific stuff. Neo and Fabric do
 * very different things, some things are separable in Fabric but they aren't in NeoForge.
 *
 * @author Hiroku
 * @since June 8th, 2024
 */
class NeoForgePacketInfo<T : NetworkPacket<T>>(val info: PacketRegisterInfo<T>) {
    fun registerToClient(registrar: PayloadRegistrar) {
        val handler = IPayloadHandler<T> { arg, _ ->
            val clientHandler = info.handler as ClientNetworkPacketHandler<T>
            clientHandler.handle(arg, Minecraft.getInstance())
        }

        registrar.playToClient(info.payloadId, info.codec, handler)
    }

    fun registerToServer(registrar: PayloadRegistrar) {
        val handler = IPayloadHandler<T> { arg, ctx ->
            val serverHandler = info.handler as ServerNetworkPacketHandler<T>
            serverHandler.handle(arg, ctx.player().server!!, ctx.player() as ServerPlayer)
        }

        registrar.playToServer(info.payloadId, info.codec, handler)
    }
}