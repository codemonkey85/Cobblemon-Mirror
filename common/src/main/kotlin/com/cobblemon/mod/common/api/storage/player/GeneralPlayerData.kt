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
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.api.storage.player.client.ClientGeneralPlayerData
import com.cobblemon.mod.common.api.storage.player.client.ClientInstancedPlayerData
import java.util.UUID
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

/**
 * An [InstancedPlayerData] for misc stuff, mostly starters
 */
data class GeneralPlayerData(
    override val uuid: UUID,
    var starterPrompted: Boolean,
    var starterLocked: Boolean,
    var starterSelected: Boolean,
    var starterUUID: UUID?,
    var keyItems: MutableSet<ResourceLocation>,
    var battleTheme: ResourceLocation?,
    val extraData: MutableMap<String, PlayerDataExtension>,
) : InstancedPlayerData {
    var advancementData: PlayerAdvancementData = PlayerAdvancementData()

    fun sendToPlayer(player: ServerPlayer) {
        player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreTypes.GENERAL, this.toClientData()))
    }

    override fun toClientData(): ClientInstancedPlayerData {
        return ClientGeneralPlayerData(
            false,
            if(Cobblemon.starterConfig.promptStarterOnceOnly) !starterPrompted else true,
            starterLocked,
            starterSelected,
            starterUUID,
            advancementData.totalBattleVictoryCount == 0,
            battleTheme
        )
    }


}
