/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

object FreezeFrameProperty : CustomPokemonPropertyType<FloatProperty> {
    override val keys = setOf("freeze_frame")
    override fun examples() = setOf("0.0", "1.5", "10")
    override val needsKey = true

    override fun fromString(value: String?): FloatProperty? {
        return buildProperty(value?.toFloatOrNull() ?: -1F)
    }

    private fun buildProperty(value: Float) = FloatProperty(
        key = keys.first(),
        value = value,
        pokemonApplicator = { _, _ -> },
        entityApplicator = { pokemonEntity, freezeFrame -> pokemonEntity.entityData.set(PokemonEntity.FREEZE_FRAME, freezeFrame) },
        pokemonMatcher = { _, _ -> false },
        entityMatcher = { pokemonEntity, freezeFrame -> pokemonEntity.entityData.get(PokemonEntity.FREEZE_FRAME) == freezeFrame }
    )
}