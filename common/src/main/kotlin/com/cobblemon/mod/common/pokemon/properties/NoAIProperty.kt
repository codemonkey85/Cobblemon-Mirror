/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType

object NoAIProperty : CustomPokemonPropertyType<BooleanProperty> {
    override val keys = setOf("no_ai")
    override val needsKey = true

    override fun fromString(value: String?) =
        when {
            value == null || value.lowercase() in listOf("true", "yes") -> noAI(true)
            value.lowercase() in listOf("false", "no") -> noAI(false)
            else -> null
        }

    fun noAI(value: Boolean) = BooleanProperty(
        key = keys.first(),
        value = value,
        pokemonApplicator = { _, _ -> },
        entityApplicator = { pokemonEntity, noAI -> pokemonEntity.isNoAi = true },
        pokemonMatcher = { _, _ -> false },
        entityMatcher = { pokemonEntity, noAI -> pokemonEntity.isNoAi == noAI }
    )

    override fun examples() = setOf("yes", "no")
}