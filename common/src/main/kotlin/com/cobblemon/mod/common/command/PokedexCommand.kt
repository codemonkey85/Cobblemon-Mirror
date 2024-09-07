/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.def.PokedexDef
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.species
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.command.argument.DexArgumentType
import com.cobblemon.mod.common.command.argument.FormArgumentType
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component

object PokedexCommand {

    private const val NAME = "pokedex"
    private const val GRANT_NAME = "grant"
    private const val REVOKE_NAME = "revoke"
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val commandArgumentBuilder = Commands.literal(NAME)
        val grantCommandBuilder = Commands.literal(GRANT_NAME).then(
            Commands.argument("player", EntityArgument.player())
                .then(Commands.literal("all")
                    .then(
                        Commands.argument("dex", DexArgumentType.dex()).executes(::executeGrantAll)
                    )
                )
                .then(Commands.literal("only").then(
                    Commands.argument("species", SpeciesArgumentType.species()).then(
                        Commands.argument("form", FormArgumentType.form()).executes(::executeGrantOnly)
                    )
                ))
        )
        val revokeCommandBuilder = Commands.literal(REVOKE_NAME).then(
            Commands.argument("player", EntityArgument.player())
                .then(Commands.literal("all").then(
                    Commands.argument("dex", DexArgumentType.dex()).executes(::executeRemoveAll)
                )
            )
            .then(Commands.literal("only").then(
                Commands.argument("species", SpeciesArgumentType.species()).then(
                    Commands.argument("form", FormArgumentType.form()).executes(::executeRemoveOnly)
                )
            ))
        )
        commandArgumentBuilder
            .then(grantCommandBuilder)
            .then(revokeCommandBuilder)
            .permission(CobblemonPermissions.POKEDEX)


        dispatcher.register(commandArgumentBuilder)
    }

    private fun executeGrantOnly(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val species = context.getArgument("species", Species::class.java)
        val form = context.getArgument("form", FormData::class.java)
        players.forEach {
            val dex = Cobblemon.playerDataManager.getPokedexData(it)

            //dex.grantedWithCommand(species, form)
            it.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, dex.toClientData()))
        }
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Granted ${species.name}-${form.formOnlyShowdownId()} to $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeRemoveOnly(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val species = context.getArgument("species", Species::class.java)
        val form = context.getArgument("form", FormData::class.java)
        players.forEach {
            val dex = Cobblemon.playerDataManager.getPokedexData(it)
            //dex.removedWithCommand(species, form)
            it.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, dex.toClientData()))
        }
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Removed ${species.name}-${form.formOnlyShowdownId()} from $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }
    private fun executeGrantAll(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val dexDef = context.getArgument("dex", PokedexDef::class.java)
        players.forEach { player ->
            val dex = Cobblemon.playerDataManager.getPokedexData(player)
            dexDef.getEntries().forEach { dexEntry ->
                val speciesRecord = dex.getOrCreateSpeciesRecord(dexEntry.speciesId)
                dexEntry.forms.forEach { form ->
                    form.unlockForms.forEach {unlockForm ->
                        val formRecord = speciesRecord.getOrCreateFormRecord(unlockForm)
                        formRecord.setKnowledgeProgress(PokedexEntryProgress.CAUGHT)
                        formRecord.addAllShinyStatesAndGenders()
                    }
                }
                speciesRecord.addAspects(dexEntry.variations.flatMap { it.aspects }.toSet())
            }
            player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, dex.toClientData()))
        }
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Filled dex of $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeRemoveAll(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        players.forEach {
            val dex = Cobblemon.playerDataManager.getPokedexData(it)
            //FIXME
            //dex.removedByCommand(null, null)
            it.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, dex.toClientData()))
        }
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Cleared dex of $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }
}