/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin;

import com.cobblemon.mod.common.api.net.UnsplittablePacket;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.neoforge.network.filters.GenericPacketSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * See {@link UnsplittablePacket} for why this is necessary
 * I am concerned about how mappings will work with this, as the CustomPayloadS2CPacket stuff needs to be remapped
 * NeoForge stuff doesnt...
 */
@Mixin(GenericPacketSplitter.class)
public class GenericPacketSplitterMixin {

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Ljava/util/List;)V", at=@At("HEAD"), cancellable = true)
    public void encode(ChannelHandlerContext channelHandlerContext, Packet packet, List<Object> list, CallbackInfo ci) {
        if (packet instanceof ClientboundCustomPayloadPacket) {
            ClientboundCustomPayloadPacket pac = (ClientboundCustomPayloadPacket) packet;
            if (((ClientboundCustomPayloadPacket) packet).payload() instanceof UnsplittablePacket) {
                list.add(packet);
                ci.cancel();
                return;
            }
        }
        if (packet instanceof ServerboundCustomPayloadPacket) {
            ServerboundCustomPayloadPacket pac = (ServerboundCustomPayloadPacket) packet;
            if (((ServerboundCustomPayloadPacket) packet).payload() instanceof UnsplittablePacket) {
                list.add(packet);
                ci.cancel();
                return;
            }
        }
    }
}
