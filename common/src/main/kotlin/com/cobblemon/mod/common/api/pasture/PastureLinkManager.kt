/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pasture

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.net.messages.client.pasture.ClosePasturePacket
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.removeIf
import net.minecraft.core.BlockPos
import java.util.UUID
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel

// Need to use this in the interact with pasture block then use links when handling server-side packets
// to authenticate that they were able to interact.
object PastureLinkManager {
    // Maps player UUID
    val links = mutableMapOf<UUID, PastureLink>()

    fun getLinkByPlayerId(playerId: UUID) = links[playerId]
    fun createLink(playerId: UUID, link: PastureLink) {
        links[playerId] = link
    }

    fun getLinkByPlayer(player: ServerPlayer): PastureLink? {
        val link = getLinkByPlayerId(player.uuid)
        if (link != null) {
            if (!player.level().dimensionTypeRegistration().`is`(link.dimension) || !link.pos.closerToCenterThan(player.position(), 10.0)) {
                links.remove(player.uuid)
                return null
            }
        }

        return link
    }

    fun removeAt(world: ServerLevel, pos: BlockPos) {
        links.removeIf { (uuid, pastureLink) ->
            val shouldRemove = world.dimensionTypeRegistration().`is`(pastureLink.dimension) && pastureLink.pos == pos
            uuid.getPlayer()?.sendPacket(ClosePasturePacket())
            return@removeIf shouldRemove
        }
    }
}