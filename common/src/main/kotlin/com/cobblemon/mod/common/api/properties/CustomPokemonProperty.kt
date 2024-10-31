/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty.Companion.register
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider
import com.cobblemon.mod.common.util.server

/**
 * A custom property that can be parsed from a string and applied/matched against
 * a [Pokemon] and [PokemonEntity]. To register your own custom property, use
 * any of the [register] method overloads.
 *
 * @author Hiroku
 * @since February 12th, 2022
 */
interface CustomPokemonProperty {
    companion object {
        /** A list of all the registered [CustomPokemonPropertyType]s. */
        val properties = mutableListOf<CustomPokemonPropertyType<*>>()

        fun <T: CustomPokemonProperty> register(propertyType: CustomPokemonPropertyType<T>) {
            properties.add(propertyType)
            this.triggerSyncAttempt()
        }

        fun <T : CustomPokemonProperty> register(name: String, needsLabel: Boolean = true, fromString: (String?) -> T?, examples: () -> Collection<String>) {
            register(listOf(name), needsLabel, fromString, examples)
        }

        fun <T : CustomPokemonProperty> register(aliases: Iterable<String>, needsLabel: Boolean = true, fromString: (String?) -> T?, examples: () -> Collection<String>) {
            properties.add(
                object : CustomPokemonPropertyType<T> {
                    override val keys = aliases
                    override val needsKey = needsLabel
                    override fun fromString(value: String?) = fromString(value)
                    override fun examples() = examples.invoke()
                }
            )
            this.triggerSyncAttempt()
        }

        fun unregister(property: CustomPokemonPropertyType<*>) {
            properties.remove(property)
        }

        // We do this every time a new property is registered if the server is running in order to synchronize all players with the new property for tab completion purposes
        private fun triggerSyncAttempt() {
            val server = server() ?: return
            if (!server.isSingleplayer) {
                PropertiesCompletionProvider.reload()
                server.playerList.players.forEach { player -> PropertiesCompletionProvider.sync(player) }
            }
        }
    }

    /** Maps a property into the string form that would be used to create it anew. This is used for serialization. */
    fun asString(): String
    /** Applies this property to a [Pokemon]. */
    fun apply(pokemon: Pokemon)
    /**
     * Applies this property to a [PokemonEntity]. By default this just assumes that the [apply] method taking a
     * [Pokemon] is all this needs to run.
     */
    fun apply(pokemonEntity: PokemonEntity) = apply(pokemonEntity.pokemon)
    /**
     * Returns true if this property appears to be set on the given [Pokemon]. In some properties this should always
     * return true (if the property isn't something that is usable as a filter.
     */
    fun matches(pokemon: Pokemon): Boolean
    /**
     * Returns true if this property appears to be set on the given [PokemonEntity]. In some properties this
     * should always return true (if the property isn't something that is usable as a filter. The default implementation
     * of this function assumes that the [matches] method taking a [Pokemon] is sufficient.
     */
    fun matches(pokemonEntity: PokemonEntity): Boolean = matches(pokemonEntity.pokemon)
}