/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.factory

import com.cobblemon.mod.common.api.storage.player.PlayerData
import java.util.UUID

interface PlayerDataStoreFactory {

    fun load(uuid: UUID) : PlayerData
    fun save(data: PlayerData)
    fun saveAll()
    fun onPlayerDisconnect(uuid: UUID)

}