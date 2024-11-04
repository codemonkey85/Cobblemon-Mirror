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
import com.cobblemon.mod.common.command.argument.NPCClassArgumentType
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.toBlockPos
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

object SpawnNPCCommand {
    private const val NAME = "spawnnpc"
    private const val CLASS = "class"
    private const val POSITION = "pos"
    private const val LEVEL = "level"
    private const val ALIAS = "npcspawn"
    private const val AT_NAME = "${NAME}at"
    private const val AT_ALIAS = "${ALIAS}at"
    // ToDo maybe dedicated lang down the line but the errors shouldn't really happen unless people are really messing up
    private val INVALID_POS_EXCEPTION = SimpleCommandExceptionType(Component.literal("Invalid position").red())
    private val FAILED_SPAWN_EXCEPTION = SimpleCommandExceptionType(Component.literal("Unable to spawn at the given position").red())

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val contextPositionCommand = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.SPAWN_NPC)
            .then(argument(CLASS, NPCClassArgumentType.npcClass())
                .then(
                    argument(LEVEL, IntegerArgumentType.integer(1))
                        .executes { context -> execute(context, context.source.position, IntegerArgumentType.getInteger(context, LEVEL)) }
                )
                .executes { context -> execute(context, context.source.position, 1) }
            )
        )
        dispatcher.register(contextPositionCommand.alias(ALIAS))
        val argumentPositionCommand = dispatcher.register(literal(AT_NAME)
            .permission(CobblemonPermissions.SPAWN_NPC)
            .then(argument(POSITION, Vec3Argument.vec3())
                .then(argument(CLASS, NPCClassArgumentType.npcClass())
                    .then(
                        argument(LEVEL, IntegerArgumentType.integer(1))
                            .executes { context -> execute(context, Vec3Argument.getVec3(context, POSITION), IntegerArgumentType.getInteger(context, LEVEL)) }
                    )
                    .executes { context -> execute(context, Vec3Argument.getVec3(context, POSITION), 1) }
                )
            )
        )
        dispatcher.register(argumentPositionCommand.alias(AT_ALIAS))
    }

    private fun execute(context: CommandContext<CommandSourceStack>, pos: Vec3, level: Int): Int {
        val world = context.source.level
        val blockPos = pos.toBlockPos()
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POS_EXCEPTION.create()
        }
        val npcClass = NPCClassArgumentType.getNPCClass(context, CLASS)
        val npc = NPCEntity(world)
        npc.moveTo(pos.x, pos.y, pos.z, npc.yRot, npc.xRot)
        npc.npc = npcClass
        npc.initialize(level)
        if (world.addFreshEntity(npc)) {
            return Command.SINGLE_SUCCESS
        }
        throw FAILED_SPAWN_EXCEPTION.create()
    }
}