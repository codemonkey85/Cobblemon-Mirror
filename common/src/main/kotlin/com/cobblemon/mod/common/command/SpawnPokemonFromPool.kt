/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.spawning.CobblemonWorldSpawnerManager
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.context.AreaSpawningContext
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.effectiveName
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal

/**
 * Spawn Pokemon From Surrounding Pool
 *
 * `/spawnpokemonfrompool [amount]` or the alias `/forcespawn [amount]`
 *
 * This command can fail if the randomly selection spawn region has no possible [AreaSpawningContext]. For example if
 *   you are flying in the air
 */
object SpawnPokemonFromPool {
    const val NAME = "spawnpokemonfrompool"
    const val ALIAS = "forcespawn"

    private val UNABLE_TO_SPAWN = commandLang("spawnpokemonfrompool.unable_to_spawn")

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val spawnPokemonFromPoolCommand = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.SPAWN_POKEMON)
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes { context -> execute(context, IntegerArgumentType.getInteger(context, "amount")) }
            )
            .executes { context -> execute(context, 1) }
        )

        dispatcher.register(spawnPokemonFromPoolCommand.alias(ALIAS))
    }

    private fun execute(context: CommandContext<CommandSourceStack>, amount: Int): Int {
        val player = context.source.playerOrException
        val spawner = CobblemonWorldSpawnerManager.spawnersForPlayers.getValue(player.uuid)

        var spawnsTriggered = 0

        // This could instead directly use a [Spawner] method if refactored, as it is currently it has
        //   entity counting coupled to the generation of a entity to spawn. Might be a good future change?
        for (i in 1..amount) {
            val spawnCause = SpawnCause(spawner = spawner, bucket = spawner.chooseBucket(), entity = spawner.getCauseEntity())

            val area = spawner.getArea(spawnCause) ?: continue
            val slice = spawner.prospector.prospect(spawner, area)
            val contexts = spawner.resolver.resolve(spawner, spawner.contextCalculators, slice)

            // This has a chance to fail, if you get a "slice" that has no associated contexts.
            //   but as it was selected at random by the Prospector, it could just be a miss which
            //   means two attempts to spawn in the same location can have differing results (which is expected for
            //   randomness).
            if (contexts.isEmpty()) {
                player.sendSystemMessage(UNABLE_TO_SPAWN.red())
                continue
            }

            val result = spawner.getSpawningSelector().select(spawner, contexts)
            if (result == null) {
                player.sendSystemMessage(UNABLE_TO_SPAWN.red())
                continue
            }

            val spawnAction = result.second.doSpawn(ctx = result.first)

            spawnAction.future.thenApply {
                if (it is EntitySpawnResult) {
                    for (entity in it.entities) {
                        player.sendSystemMessage(commandLang("spawnpokemonfrompool.success", entity.effectiveName()).green())
                    }
                }
            }

            spawnAction.complete()
            spawnsTriggered++
        }

        return spawnsTriggered
    }
}