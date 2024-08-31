/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.PacketHandler
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * A generic wrapping of packet information for registration.
 *
 * @author Apion, Hiroku
 * @since June 7th, 2024
 */
class PacketRegisterInfo<T : NetworkPacket<T>>(
    val id: ResourceLocation,
    val decoder: (RegistryFriendlyByteBuf) -> T,
    val handler: PacketHandler<T>,
    codec: StreamCodec<RegistryFriendlyByteBuf, T>? = null
) {
    val payloadId = CustomPacketPayload.Type<T>(id)
    val codec = codec ?: StreamCodec.of(
        { buf, packet -> packet.encode(buf) },
        decoder
    )
}