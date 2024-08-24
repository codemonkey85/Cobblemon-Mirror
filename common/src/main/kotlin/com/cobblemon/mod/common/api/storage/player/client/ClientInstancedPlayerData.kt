/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.client

import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * The information a client needs to know about an [InstancedPlayerData]
 *
 * There might be a temptation to move these into the client package, but the server needs to be aware that these exist
 * So they can be put in the client bound packet
 *
 * @since February 21, 2024
 * @author Apion
 */
interface ClientInstancedPlayerData {
    fun encode(buf: RegistryFriendlyByteBuf)
}