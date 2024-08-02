/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import net.minecraft.world.entity.ai.goal.Goal
import java.util.*

/**
 * AI goal for sleeping in the wild.
 *
 * @author Hiroku
 * @since July 18th, 2022
 */
class WildRestGoal(private val pokemonEntity: PokemonEntity) : Goal() {
    override fun getFlags(): EnumSet<Flag> = EnumSet.allOf(Flag::class.java)

    override fun canUse(): Boolean {
        val rest = pokemonEntity.behaviour.resting
        if (!pokemonEntity.pokemon.isWild() || pokemonEntity.random.nextFloat() < 1 - rest.sleepChance || !pokemonEntity.canSleep() || pokemonEntity.isBusy || !rest.depth.canSleep(pokemonEntity)) {
            return false
        }
        return true
    }

    override fun isInterruptable(): Boolean {
        return false
    }

    override fun canContinueToUse(): Boolean {
        return if (pokemonEntity.canSleep() && !pokemonEntity.behaviour.resting.depth.shouldWake(pokemonEntity)) {
            true
        } else {
            wake()
            false
        }
    }

    override fun start() {
        pokemonEntity.pokemon.status = PersistentStatusContainer(Statuses.SLEEP)
    }

    fun wake() {
        if (pokemonEntity.battleId == null) {
            pokemonEntity.pokemon.status = null
        }
    }
}