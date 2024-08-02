/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive.ability

import com.cobblemon.mod.common.api.abilities.*
import com.cobblemon.mod.common.api.item.ability.AbilityChanger
import com.cobblemon.mod.common.pokemon.Pokemon


// TODO Polish me down the line when we actually release the item in the mod, we need a way to select the ability
open class AbilityTypeChanger<T : PotentialAbility>(
    override val type: PotentialAbilityType<T>,
    private val supportsChangingFrom: (other: PotentialAbilityType<*>?) -> Boolean
) : AbilityChanger<T> {

    override fun queryPossible(pokemon: Pokemon): Set<AbilityTemplate> {
        val ofType = pokemon.form.abilities.filter { it.type == this.type }
        return ofType.filter { it.template != pokemon.ability.template }
            .map { it.template }
            .toSet()
    }

    override fun performChange(pokemon: Pokemon): Boolean {
        val currentType = this.findCurrent(pokemon)
        if (!this.canChangeFrom(currentType)) {
            return false
        }
        val possible = this.queryPossible(pokemon)
        val picked = possible.randomOrNull() ?: return false
        val old = pokemon.ability.template
        pokemon.updateAbility(picked.create(forced = false))
        return pokemon.ability.template != old
    }

    override fun canChangeFrom(type: PotentialAbilityType<*>?): Boolean = this.supportsChangingFrom(type)

    private fun findCurrent(pokemon: Pokemon): PotentialAbilityType<*>? {
        if (pokemon.ability.forced) {
            return null
        }
        return pokemon.form.abilities.firstOrNull { it.template == pokemon.ability.template }?.type
    }

}