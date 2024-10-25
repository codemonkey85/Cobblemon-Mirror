/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ChangeEyeHeight {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal("changeeyeheight")
            .permission(CobblemonPermissions.CHANGE_EYE_HEIGHT)
            .then(
                Commands.argument("pokemon", SpeciesArgumentType.species())
                    .then(
                        Commands.literal("standing").then(Commands.argument("height", FloatArgumentType.floatArg())
                            .executes { setEyeHeight(it) { species, height -> species.standingEyeHeight = height } })
                    )
                    .then(
                        Commands.literal("flying").then(Commands.argument("height", FloatArgumentType.floatArg())
                            .executes { setEyeHeight(it) { species, height -> species.flyingEyeHeight = height } })
                    )
                    .then(
                        Commands.literal("swimming").then(Commands.argument("height", FloatArgumentType.floatArg())
                            .executes { setEyeHeight(it) { species, height -> species.swimmingEyeHeight = height } })
                    ))
        dispatcher.register(command)
    }

    private fun setEyeHeight(context: CommandContext<CommandSourceStack>, applicator: (species: Species, height: Float) -> Unit) : Int {
        val pkm = SpeciesArgumentType.getPokemon(context, "pokemon")
        val height = FloatArgumentType.getFloat(context, "height")

        applicator.invoke(pkm, height)
        pkm.forms.clear()
        pkm.forms.add(FormData().also { it.initialize(pkm) })
        return Command.SINGLE_SUCCESS
    }
}