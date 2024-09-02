/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.starter

import com.cobblemon.mod.common.config.starter.StarterCategory
import net.minecraft.server.level.ServerPlayer

interface StarterHandler {
    fun getStarterList(player: ServerPlayer): List<StarterCategory>
    fun handleJoin(player: ServerPlayer)
    fun requestStarterChoice(player: ServerPlayer)
    fun chooseStarter(player: ServerPlayer, categoryName: String, index: Int)
}