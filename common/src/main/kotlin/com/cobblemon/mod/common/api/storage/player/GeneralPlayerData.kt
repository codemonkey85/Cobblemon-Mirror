/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.storage.player.client.ClientGeneralPlayerData
import com.cobblemon.mod.common.api.storage.player.client.ClientInstancedPlayerData
import com.cobblemon.mod.common.net.messages.client.starter.SetClientPlayerDataPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.UUID

/**
 * An [InstancedPlayerData] for misc stuff, mostly starters
 */
data class GeneralPlayerData(
    override val uuid: UUID,
    var starterPrompted: Boolean,
    var starterLocked: Boolean,
    var starterSelected: Boolean,
    var starterUUID: UUID?,
    var keyItems: MutableSet<Identifier>,
    val extraData: MutableMap<String, PlayerDataExtension>,
) : InstancedPlayerData {
    var advancementData: PlayerAdvancementData = PlayerAdvancementData()

    fun sendToPlayer(player: ServerPlayerEntity) {
        player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.GENERAL, this.toClientData()))
    }

    override fun toClientData(): ClientInstancedPlayerData {
        return ClientGeneralPlayerData(
            false,
            starterPrompted || !Cobblemon.starterConfig.promptStarterOnceOnly,
            starterPrompted,
            starterLocked,
            starterUUID
        )
    }

}