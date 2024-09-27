/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.storage.player.GeneralPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * A [PlayerDataStoreBackend] for [GeneralPlayerData]
 *
 * @author Apion
 * @since February 21, 2024
 */
class PlayerDataJsonBackend: JsonBackedPlayerDataStoreBackend<GeneralPlayerData>(
    "cobblemonplayerdata", PlayerInstancedDataStoreTypes.GENERAL
){
    override val defaultData = { forPlayer: UUID -> GeneralPlayerData(
        uuid = forPlayer,
        starterPrompted = false,
        starterLocked = !Cobblemon.starterConfig.allowStarterOnJoin,
        starterSelected =  false,
        starterUUID =  null,
        keyItems = mutableSetOf(),
        extraData = mutableMapOf(),
        battleTheme = CobblemonSounds.PVP_BATTLE.location
    )}

    override val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapter(PlayerDataExtension::class.java, PlayerDataExtensionAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()

    override val classToken = TypeToken.get(GeneralPlayerData::class.java)
}