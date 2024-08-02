/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.permission

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.Permission
import com.cobblemon.mod.common.api.permission.PermissionValidator
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer

class FabricPermissionValidator : PermissionValidator {
    override fun initialize() {
        Cobblemon.LOGGER.info("Booting FabricPermissionValidator, permissions will be checked using fabric-permissions-api, see https://github.com/lucko/fabric-permissions-api")
    }

    override fun hasPermission(player: ServerPlayer, permission: Permission) = Permissions.check(player, permission.literal, permission.level.numericalValue)

    override fun hasPermission(source: CommandSourceStack, permission: Permission) = Permissions.check(source, permission.literal, permission.level.numericalValue)
}