/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType

/**
 * A type of [CustomPokemonPropertyType] handling a [FlagProperty] which, when
 * present, indicates that the Pokémon is a battle clone and should not
 * persist outside the context of a battle.
 *
 */
object BattleCloneProperty : CustomPokemonPropertyType<FlagProperty> {
    override val keys = setOf("battleClone")
    override val needsKey = true

    override fun fromString(value: String?) =
        when {
            value == null || value.lowercase() in listOf("true", "yes") -> isBattleClone()
            value.lowercase() in listOf("false", "no") -> { FlagProperty(keys.first(), false) }
            else -> null
        }

    fun isBattleClone() = FlagProperty(keys.first(), false)

    override fun examples() = setOf("yes", "no")
}