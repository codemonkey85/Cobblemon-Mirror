/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.OriginalTrainerType
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.simplify

internal object BuiltinPropertiesCompletionProvider : PropertiesCompletionProvider() {

    init {
        this.registerBuiltin()
    }

    private fun registerBuiltin() {
        this.inject(setOf("level", "lvl", "l"), setOf("1", "${Cobblemon.config.maxPokemonLevel}") )
        this.inject(setOf("shiny", "s"), setOf("yes", "no"))
        this.inject("gender", Gender.entries.map { it.name.lowercase() })
        this.inject("friendship", setOf("0", Cobblemon.config.maxPokemonFriendship.toString()))
        this.inject("pokeball", PokeBalls.all().map { it.name.simplify() })
        this.inject("nature", Natures.all().map { it.name.simplify() })
        this.inject("ability", CobblemonRegistries.ABILITY.entrySet().map { it.key.location().simplify() })
        this.inject("dmax", setOf("0", Cobblemon.config.maxDynamaxLevel.toString()))
        this.inject("gmax", setOf("yes", "no"))
        this.inject(setOf("type", "elemental_type"), CobblemonRegistries.ELEMENTAL_TYPE.entrySet().map { it.key.location().simplify() })
        this.inject(setOf("tera_type", "tera"), CobblemonRegistries.ELEMENTAL_TYPE.entrySet().map { it.key.location().simplify() })
        this.inject("tradeable", setOf("yes", "no"))
        this.inject(setOf("originaltrainer", "ot"), emptySet())
        this.inject(setOf("originaltrainertype", "ottype"), OriginalTrainerType.entries.map { it.name }.toSet())

        Stats.PERMANENT.forEach{ stat ->
            val statName = stat.toString().lowercase()
            this.inject(setOf("${statName}_iv"), setOf("0", IVs.MAX_VALUE.toString()))
            this.inject(setOf("${statName}_ev"), setOf("0", EVs.MAX_STAT_VALUE.toString()))
        }

        this.inject(setOf("status"), Statuses.getPersistentStatuses().map { it.name.simplify() })
    }

}