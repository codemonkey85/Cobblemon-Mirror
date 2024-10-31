/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.permission

//class LuckPermsPermissionValidator : PermissionValidator {
//
//    private val luckPerms by lazy { LuckPermsProvider.get() }
//
//    override fun initiate() {
//        PokemonCobblemon.LOGGER.info("Booting LuckPermsPermissionValidator, permissions will be checked through LuckPerms, see https://luckperms.net/ for more information")
//    }
//
//    override fun hasPermission(player: ServerPlayer, permission: String) = this.luckPerms.userManager.getUser(player.uuid)?.cachedData?.permissionData?.checkPermission(permission)?.asBoolean() ?: false
//
//    override fun hasPermission(source: CommandSource, permission: String): Boolean {
//        val serverSource = source as? ServerCommandSource ?: return true
//        val player = source.entity as? ServerPlayer ?: return true
//        return this.hasPermission(player, permission)
//    }
//
//}