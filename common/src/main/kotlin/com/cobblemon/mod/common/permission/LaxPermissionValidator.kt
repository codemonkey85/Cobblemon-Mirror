/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.permission

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.Permission
import com.cobblemon.mod.common.api.permission.PermissionValidator
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer

/**
 * A [PermissionValidator] that uses the permission level vanilla system.
 * This is only used when the platform has no concept of permissions.
 */
class LaxPermissionValidator : PermissionValidator {

    override fun initialize() {
        Cobblemon.LOGGER.info("Booting LaxPermissionValidator, permissions will be checked using Minecrafts permission level system, see https://minecraft.fandom.com/wiki/Permission_level")
    }

    override fun hasPermission(player: ServerPlayer, permission: Permission) = player.hasPermissions(permission.level.numericalValue)
    override fun hasPermission(source: CommandSourceStack, permission: Permission) = source.hasPermission(permission.level.numericalValue)
}