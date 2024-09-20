/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.requiresWithPermission
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

object FreezePokemonCommand {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("freezepokemon")
                .requiresWithPermission(CobblemonPermissions.FREEZE_POKEMON) { it.player != null }
                .then(
                    Commands
                        .argument("seconds", FloatArgumentType.floatArg(-1F))
                        .executes { execute(it, it.source.playerOrException, freezeFrame = FloatArgumentType.getFloat(it, "seconds")) }
                )
                .executes { execute(it, it.source.playerOrException) }
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer, freezeFrame: Float = 0F) : Int {
        val targetEntity = player.traceFirstEntityCollision(entityClass = PokemonEntity::class.java)
        if (targetEntity == null) {
            player.sendSystemMessage(commandLang("freezepokemon.non_pokemon").red())
            return 0
        }

        targetEntity.entityData.set(PokemonEntity.FREEZE_FRAME, freezeFrame.takeIf { it >= 0F || it == -1F } ?: -1F)
        return Command.SINGLE_SUCCESS
    }
}