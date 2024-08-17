/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pasture

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import net.minecraft.server.level.ServerPlayer

/**
 * Interface for a route to which a player might be permitted to use a pasture block. This is registered
 * in [PasturePermissionControllers] with a priority.
 *
 * @author Hiroku
 * @since July 2nd, 2023
 */
fun interface PasturePermissionController {
    fun permit(player: ServerPlayer, pastureBlockEntity: PokemonPastureBlockEntity): PasturePermissions?
}