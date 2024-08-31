/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.command.*
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object CobblemonCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registry: CommandBuildContext, selection: Commands.CommandSelection) {
        SpawnPokemon.register(dispatcher)
        GivePokemon.register(dispatcher)
        TakePokemon.register(dispatcher)
        ChangeScaleAndSize.register(dispatcher)
        ChangeWalkSpeed.register(dispatcher)
        TestCommand.register(dispatcher)
        ReloadShowdownCommand.register(dispatcher)
        ClickTextCommand.register(dispatcher)
        PokemonEditCommand.register(dispatcher)
        TeachCommand.register(dispatcher)
        LevelUp.register(dispatcher)
        HealPokemonCommand.register(dispatcher)
        StopBattleCommand.register(dispatcher)
        CheckSpawnsCommand.register(dispatcher)
        GetNBT.register(dispatcher)
        OpenStarterScreenCommand.register(dispatcher)
        SpawnAllPokemon.register(dispatcher)
        FriendshipCommand.register(dispatcher)
        GiveAllPokemon.register(dispatcher)
        HeldItemCommand.register(dispatcher, registry)
        PcCommand.register(dispatcher)
        SpawnPokemonFromPool.register(dispatcher)
        PokeboxCommand.register(dispatcher)
        TestStoreCommand.register(dispatcher)
        QueryLearnsetCommand.register(dispatcher)
        TestPcSlotCommand.register(dispatcher)
        TestPartySlotCommand.register(dispatcher)
        ClearPartyCommand.register(dispatcher)
        ClearPCCommand.register(dispatcher)
        PokemonRestartCommand.register(dispatcher)
        BedrockParticleCommand.register(dispatcher)
        OpenDialogueCommand.register(dispatcher)
        NPCEditCommand.register(dispatcher)

        // Possibly lock down registration if and only if under dev environment or running in an environment
        // with a certain system environment variable set
        CobblemonInfoCommand.register(dispatcher)
    }
}