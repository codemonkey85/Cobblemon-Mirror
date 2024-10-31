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
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ChangeWalkSpeed {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal("changewalkspeed")
            .permission(CobblemonPermissions.CHANGE_WALK_SPEED)
            .then(
                Commands.argument("pokemon", SpeciesArgumentType.species())
                    .then(Commands.argument("walkSpeed", FloatArgumentType.floatArg()).executes(::execute))
            )

            .executes(::execute)
        dispatcher.register(command)
    }



    private fun execute(context: CommandContext<CommandSourceStack>) : Int {
        val pkm = SpeciesArgumentType.getPokemon(context, "pokemon")
        val walkSpeed = FloatArgumentType.getFloat(context, "walkSpeed")

        pkm.behaviour.moving.walk.walkSpeed = walkSpeed.toString().asExpressionLike()
        pkm.forms.clear()
        pkm.forms.add(FormData().also { it.initialize(pkm)})
        return Command.SINGLE_SUCCESS
    }
}
