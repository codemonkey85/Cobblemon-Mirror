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
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.world.entity.EntityDimensions

object ChangeScaleAndSize {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal("changescaleandsize")
            .permission(CobblemonPermissions.CHANGE_SCALE_AND_SIZE)
            .then(
                Commands.argument("pokemon", SpeciesArgumentType.species())
                    .then(
                        Commands.argument("scale", FloatArgumentType.floatArg())
                            .then(Commands.argument("width", FloatArgumentType.floatArg())
                                .then(Commands.argument("height", FloatArgumentType.floatArg()).executes(::execute))
                            )
                    )

                    .executes(::execute))
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>) : Int {
        val pkm = SpeciesArgumentType.getPokemon(context, "pokemon")
        val scale = FloatArgumentType.getFloat(context, "scale")
        val width = FloatArgumentType.getFloat(context, "width")
        val height = FloatArgumentType.getFloat(context, "height")

        pkm.baseScale = scale
        pkm.hitbox = EntityDimensions.scalable(width, height)
        pkm.forms.clear()
        pkm.forms.add(FormData().also { it.initialize(pkm) })
        return Command.SINGLE_SUCCESS
    }
}