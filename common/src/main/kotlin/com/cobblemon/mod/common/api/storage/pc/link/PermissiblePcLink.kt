/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc.link

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.Permission
import com.cobblemon.mod.common.api.storage.pc.PCStore
import net.minecraft.server.level.ServerPlayer

/**
 * A [PCLink] tied to a player that must have a permission.
 *
 * @param pc The [PCStore] being opened.
 * @param player The [ServerPlayer] opening the PC.
 * @param permission The [Permission] required for this link to work.
 */
class PermissiblePcLink(pc: PCStore, player: ServerPlayer, private val permission: Permission) : PCLink(pc, player.uuid) {

    override fun isPermitted(player: ServerPlayer): Boolean {
        val result = Cobblemon.permissionValidator.hasPermission(player, this.permission)
        if (!result) {
            PCLinkManager.removeLink(player.uuid)
        }
        return result
    }

}