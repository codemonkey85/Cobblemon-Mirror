/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import java.util.UUID
import net.minecraft.network.chat.MutableComponent

/**
 * The client side representation of a Pokémon in battle.
 *
 * @property uuid
 * @property displayName
 * @property properties
 * @property aspects
 * @property hpValue The current value of the HP.
 * @property maxHp The maximum value of HP.
 * @property isHpFlat If this is a flat value, this will be true if the client is the player controlling the Pokémon or is an ally of the controller.
 * @property status
 * @property statChanges
 */
class ClientBattlePokemon(
    val uuid: UUID,
    var displayName: MutableComponent,
    var properties: PokemonProperties,
    private var aspects: Set<String>,
    var hpValue: Float,
    var maxHp: Float,
    var isHpFlat: Boolean,
    var status: PersistentStatus?,
    var statChanges: MutableMap<Stat, Int>
) {
    lateinit var actor: ClientBattleActor
    val species: Species
        get() = PokemonSpecies.getByIdentifier(properties.species!!.asIdentifierDefaultingNamespace())!!
    val level: Int
        get() = properties.level ?: 0

    val gender: Gender
        get() = properties.gender ?: Gender.GENDERLESS

    var state = FloatingState().also {
        it.currentAspects = aspects
    }

    fun updateAspects(aspects: Set<String>) {
        this.aspects = aspects
        state.currentAspects = aspects
    }
}