/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.requiresWithPermission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object GiveAllPokemon {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("giveallpokemon")
                .requiresWithPermission(CobblemonPermissions.GIVE_ALL_POKEMON) { it.player != null }
                .then(
                    Commands.argument("min", IntegerArgumentType.integer(1))
                        .then(
                            Commands.argument("max", IntegerArgumentType.integer(1))
                                .executes {
                                    execute(it, IntegerArgumentType.getInteger(it, "min")..IntegerArgumentType.getInteger(it, "max"))
                                }
                        )
                        .executes { execute(it, IntegerArgumentType.getInteger(it, "min")..Int.MAX_VALUE) }
                )
                .executes { execute(it, 1..Int.MAX_VALUE) }
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>, range: IntRange) : Int {
        val player = context.source.playerOrException
        val pc = player.party().getOverflowPC(player.registryAccess()) ?: return 0

        val orderedSpeces = PokemonSpecies.implemented.sortedBy { it.nationalPokedexNumber }

        for (species in orderedSpeces) {
            val pokemon = species.create()
            pokemon.setOriginalTrainer(player.uuid)
            pc.add(pokemon)//.sendOut(player.level() as ServerWorld, player.pos)
        }

        return Command.SINGLE_SUCCESS
    }
}