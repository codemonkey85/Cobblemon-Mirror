/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.text.textClickHandlers
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import java.util.*

object ClickTextCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("cobblemonclicktext")
                .requires { src -> src.entity is ServerPlayer }
                .then(
                    RequiredArgumentBuilder
                        .argument<CommandSourceStack, String>("callback", StringArgumentType.greedyString())
                        .executes { ctx ->
                            val player = ctx.source.entity as ServerPlayer
                            textClickHandlers[UUID.fromString(ctx.getArgument("callback", String::class.java))]?.invoke(player)
                            return@executes 1
                        }
                )
        )
    }
}