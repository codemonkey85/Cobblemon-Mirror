/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.api.storage.player.client.ClientInstancedPlayerData
import java.util.UUID

/**
 * Data that is per-player
 *
 * @since February 21, 2024
 * @author Apion
 */
interface InstancedPlayerData {
    //Player uuid
    val uuid: UUID

    fun initialize() {}
    //Converts this to the client version of this data
    fun toClientData(): ClientInstancedPlayerData
}