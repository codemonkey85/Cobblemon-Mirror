/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.trade

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.trade.AcceptTradeRequestPacket
import com.cobblemon.mod.common.trade.TradeManager
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ClipContext

object AcceptTradeRequestHandler : ServerNetworkPacketHandler<AcceptTradeRequestPacket> {
    override fun handle(packet: AcceptTradeRequestPacket, server: MinecraftServer, player: ServerPlayer) {
        if (player.isSpectator) return
        // Check range and line of sight
        val request = TradeManager.requests.find { it.requestID == packet.tradeOfferId }
        val otherPlayer = request?.senderId?.getPlayer() ?: return
        if (player.traceFirstEntityCollision(
            entityClass = LivingEntity::class.java,
            ignoreEntity = player,
            maxDistance = Cobblemon.config.entityInteractionMaxDistance,
            collideBlock = ClipContext.Fluid.NONE
        ) != otherPlayer) return
        TradeManager.acceptTradeRequest(player, packet.tradeOfferId)
    }
}