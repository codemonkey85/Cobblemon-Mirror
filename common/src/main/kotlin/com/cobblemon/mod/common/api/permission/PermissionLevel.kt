/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.permission

/**
 * Represents the different permission levels used in Minecraft.
 * See the Minecraft Wiki [entry](https://minecraft.fandom.com/wiki/Permission_level) for more information.
 * This is mean as a human friendly util over the obfuscated fields in [Commands].
 *
 * @author Licious
 * @since September 25th, 2022
 */
enum class PermissionLevel(val numericalValue: Int) {

    NONE(0),
    SPAWN_PROTECTION_BYPASS(1),
    CHEAT_COMMANDS_AND_COMMAND_BLOCKS(2),
    MULTIPLAYER_MANAGEMENT(3),
    ALL_COMMANDS(4)

}