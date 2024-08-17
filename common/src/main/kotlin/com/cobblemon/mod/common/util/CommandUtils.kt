/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack

fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.appendRequirement(requirement: (src: CommandSourceStack) -> Boolean): T = this.requires(this.requirement.and(requirement))

// Taken from the Velocity project
/**
 * Clones this node into a new one with the given alias.
 *
 * @param S The type of the command source.
 * @param alias The alias for the command.
 * @return A [LiteralArgumentBuilder] for the cloned command.
 */
fun <S : Any> LiteralCommandNode<S>.alias(alias: String): LiteralArgumentBuilder<S> {
    val builder = LiteralArgumentBuilder.literal<S>(alias.lowercase())
        .requires(this.requirement)
        .forward(this.redirect, this.redirectModifier, this.isFork)
        .executes(this.command)
    this.children.forEach { child ->
        builder.then(child)
    }
    return builder
}