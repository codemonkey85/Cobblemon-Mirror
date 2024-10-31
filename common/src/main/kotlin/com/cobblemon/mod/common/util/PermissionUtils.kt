/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.Permission
import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.commands.CommandSourceStack

/**
 * Creates an [ArgumentBuilder.requirement] for a permission.
 * If you'd like to apply a requirement as well from a single function use [requiresWithPermission].
 *
 * @param T the type of the [ArgumentBuilder].
 * @param permission The [Permission] for this command
 * @param appendRequirement If the existing [ArgumentBuilder.requirement] should be appended to this permission as a single predicate. Defaults to true
 * @return the [ArgumentBuilder].
 */
fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.permission(permission: Permission, appendRequirement: Boolean = true): T {
    val permissionPredicate = { src: CommandSourceStack -> Cobblemon.permissionValidator.hasPermission(src, permission)  }
    return if (appendRequirement) this.requires(this.requirement.and(permissionPredicate)) else this.requires(permissionPredicate)
}

/**
 * Creates an [ArgumentBuilder.requirement] merged with a permission.
 *
 * @param T the type of the [ArgumentBuilder].
 * @param permission The [Permission] for this command
 * @param predicate The requirement for the command.
 * @return the [ArgumentBuilder].
 */
fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresWithPermission(permission: Permission, predicate: (src: CommandSourceStack) -> Boolean): T {
    this.requires(predicate)
    return this.permission(permission)
}