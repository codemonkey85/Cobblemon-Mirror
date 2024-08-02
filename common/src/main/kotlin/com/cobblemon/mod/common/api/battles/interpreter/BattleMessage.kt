/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.interpreter

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import java.util.UUID

/**
 * A class responsible for parsing a raw simulator protocol message received from Showdown.
 * For more information see the [Showdown protocol](https://github.com/smogon/pokemon-showdown/blob/master/sim/SIM-PROTOCOL.md).
 *
 * @constructor Creates a battle message with the given raw message.
 *
 * @param rawMessage The raw message received from Showdown.
 *
 * @author Licious
 * @since December 31st, 2022
 */
class BattleMessage(rawMessage: String) {
    /**
     * The ID of the action received in the message.
     */
    var id = ""
        private set

    /**
     * The raw message received.
     */
    var rawMessage: String = rawMessage
        private set

    /**
     * A collection of the individual arguments the message had.
     */
    private val args = arrayListOf<String>()

    /**
     * A collection of the optional arguments the message had.
     */
    private val optionalArguments = hashMapOf<String, String>()

    /**
     * Pattern to match the start of an optional argument.
     */
    private val optionalArgumentMatcher = Regex("^\\$OPTIONAL_ARG_START([^]]+)$OPTIONAL_ARG_END")

    init {
        this.parse(rawMessage)
    }

    /**
     * Get an argument at the given [index].
     *
     * @param index The index of the expected argument.
     * @return The argument if existing or null.
     */
    fun argumentAt(index: Int): String? = this.args.getOrNull(index)

    /**
     * Get an optional argument with the given [name].
     * This returns the data of the argument only.
     *
     * @param name The name of the optional argument.
     * @return The argument data if existing or null.
     */
    fun optionalArgument(name: String): String? = this.optionalArguments[name.lowercase()]

    /**
     * Checks if an optional argument is present.
     * [optionalArgument] is null safe, this method is meant as a way to check 'flags' as some optional arguments contain no data.
     *
     * @param name The name of the optional argument.
     * @return True if the argument is present.
     */
    fun hasOptionalArgument(name: String): Boolean = this.optionalArgument(name) != null

    /**
     * Clears the parsed arguments and parses the message again with the new given [rawMessage].
     *
     * @param rawMessage The raw message received from Showdown.
     * @return This instance of the battle message updated.
     */
    fun parse(rawMessage: String): BattleMessage {
        var message = rawMessage.trim()
        this.id = ""
        this.args.clear()
        this.optionalArguments.clear()
        this.rawMessage = message
        if (!message.startsWith(SEPARATOR) || message == SEPARATOR) {
            return this
        }
        message = this.push(message)
        this.id = message.substringBefore(SEPARATOR)
        message = this.push(message)
        while (message.isNotBlank()) {
            val currentData = message.substringBefore(SEPARATOR)
            val optionalArgumentID = this.optionalArgumentMatcher.find(currentData)
            if (optionalArgumentID != null) {
                val id = optionalArgumentID.value.removePrefix(OPTIONAL_ARG_START).removeSuffix(OPTIONAL_ARG_END).lowercase()
                val value = currentData.substringAfter(optionalArgumentID.value).trim()
                this.optionalArguments[id] = value
            }
            else {
                this.args.add(currentData)
            }
            message = this.push(message)
        }
        return this
    }

    fun pokemonByUuid(index: Int, battle: PokemonBattle): BattlePokemon? {
        return this.argumentAt(index)?.let { UUID.fromString(it) }?.let { uuid -> battle.actors.flatMap { it.pokemonList }.find { it.uuid == uuid } }
    }

    /**
     * Queries an argument at the given [index] for a 'pnx' that will be parsed into a [BattleActor] and [ActiveBattlePokemon].
     *
     * @param index The index of the argument containing the [BattleActor] and [ActiveBattlePokemon].
     * @param battle The [PokemonBattle] being queried.
     * @return A pair of [BattleActor] and [ActiveBattlePokemon] if the argument exists and successfully parses them otherwise null.
     */
    fun actorAndActivePokemon(index: Int, battle: PokemonBattle): Pair<BattleActor, ActiveBattlePokemon>? {
        val (pnx, _) = this.pnxAndUuid(index) ?: return null
        return this.actorAndActivePokemon(pnx, battle)
    }

    /**
     * Queries an argument at the given [index] for a 'pnx' and uuid that will be parsed into a [BattlePokemon].
     *
     * @param index The index of the argument referencing the [BattlePokemon].
     * @param battle The [PokemonBattle] being queried.
     * @return The [BattlePokemon] if the argument exists and is successfully parsed; otherwise null.
     */
    fun battlePokemon(index: Int, battle: PokemonBattle): BattlePokemon? {
        val (actorID, pokemonID) = this.pnxAndUuid(index) ?: return null
        return this.battlePokemon(actorID, pokemonID, battle)
    }

    /**
     * Queries an optional argument identified by [optionalArg] for a 'pnx' and uuid that will be parsed into a [BattlePokemon].
     *
     * @param optionalArg The id of the optional argument referencing the [BattlePokemon].
     * @param battle The [PokemonBattle] being queried.
     * @return The [BattlePokemon] if the argument exists and is successfully parsed; otherwise null.
     */
    fun battlePokemonFromOptional(battle: PokemonBattle, optionalArg: String = "of"): BattlePokemon? {
        val optional = this.optionalArguments.get(optionalArg) ?: return null
        val pokemonID = optional.takeIf { it.length >= 2 }?.split(":")?.takeIf { it.size == 2 } ?: return null
        val pnx = pokemonID[0].takeIf { it.matches(PNX_MATCHER) || it.matches(PN_MATCHER) } ?: return null
        val uuid = pokemonID[1].trim()
        return this.battlePokemon(pnx, uuid, battle)
    }

    /**
     * Deconstructs the Showdown ID of a Pokemon at the given [index] into its 'pnx' and 'uuid' parts.
     *
     * @param index The index of the argument containing the Showdown ID of a Pokemon.
     * @return A 'pnx' String representing position and a 'uuid' String representing the unique Pokemon if parsed correctly, otherwise null.
     */
    fun pnxAndUuid(index: Int): Pair<String, String>? {
        val argument = this.argumentAt(index)?.takeIf { it.length >= 2 }?.split(":")?.takeIf { it.size == 2 } ?: return null
        val pnx = argument[0].takeIf { it.matches(PNX_MATCHER) || it.matches(PN_MATCHER) } ?: return null
        val uuid = argument[1].trim()
        return pnx to uuid
    }



    /**
     * Attempts to parse an [Effect] from an argument at the given [index].
     *
     * @param index The index of the expected argument.
     * @return The parsed [Effect] or null.
     */
    fun effectAt(index: Int): Effect? {
        val data = this.argumentAt(index) ?: return null
        return Effect.parse(data)
    }

    /**
     * Attempts to parse an [Effect] from an optional argument.
     *
     * @param argumentName The name of the optional argument.
     * @return The parsed [Effect] or null.
     */
    fun effect(argumentName: String = "from"): Effect? {
        val data = this.optionalArgument(argumentName) ?: return null
        return Effect.parse(data)
    }

    fun moveAt(index: Int): MoveTemplate? {
        val argument = argumentAt(index)?.lowercase()?.replace("[^a-z0-9]".toRegex(), "") ?: return null
        return Moves.getByName(argument)
    }

    /**
     * Queries an optional argument with given [argumentName] for a 'pnx' that will be parsed into a [BattleActor] and [ActiveBattlePokemon].
     *
     * @param battle The [PokemonBattle] being queried.
     * @param argumentName The name of the optional argument.
     * @return A pair of [BattleActor] and [ActiveBattlePokemon] if the argument exists and successfully parses them otherwise null.
     */
    fun actorAndActivePokemonFromOptional(battle: PokemonBattle, argumentName: String = "of"): Pair<BattleActor, ActiveBattlePokemon>? {
        val pnx = this.optionalArgument(argumentName)?.takeIf { it.length >= 3 }?.substring(0, 3) ?: return null
        return this.actorAndActivePokemon(pnx, battle)
    }

    /**
     * Pushes the given string down into the next argument.
     *
     * @param message The current state of the message.
     * @return A substring of [message] after the first [SEPARATOR] or empty if no [SEPARATOR] remains.
     */
    private fun push(message: String): String = message.substringAfter(SEPARATOR, "")

    /**
     * A utility to wrap [PokemonBattle.getActorAndActiveSlotFromPNX] to make it nullable instead of throwing an exception.
     *
     * @param pnx The raw pnx.
     * @param battle The [PokemonBattle] being queried.
     * @returnA pair of [BattleActor] and [ActiveBattlePokemon] if [PokemonBattle.getActorAndActiveSlotFromPNX] executes without any exception being thrown or else null.
     */
    private fun actorAndActivePokemon(pnx: String, battle: PokemonBattle): Pair<BattleActor, ActiveBattlePokemon>? = try {
        battle.getActorAndActiveSlotFromPNX(pnx)
    } catch (_: Exception) {
        null
    }

    private fun battlePokemon(pnx: String, pokemonID: String, battle: PokemonBattle): BattlePokemon? = try {
        battle.getBattlePokemon(pnx, pokemonID)
    } catch (_: Exception) {
        null
    }

    companion object {

        private const val SEPARATOR = "|"
        private const val OPTIONAL_ARG_START = "["
        private const val OPTIONAL_ARG_END = "]"

        /**
         * Pattern to match a Showdown position e.g. p2a, p1b
         */
        val PNX_MATCHER = Regex("p\\d[a-c]")

        /**
         * Pattern to match a Showdown side position e.g. p2, p1
         */
        val PN_MATCHER = Regex("p\\d")
    }
}