/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.util.DataKeys
import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Dynamic

class IvEvToIdentifierFix(outputSchema: Schema) : PokemonFix(outputSchema) {

    private val stringToStat = Stats.entries.associate {
        it.identifier.path to it.identifier
    }

    private val keys = setOf(DataKeys.POKEMON_IVS, DataKeys.POKEMON_EVS)

    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        var baseDynamic = dynamic
        this.keys.forEach { key ->
            if (baseDynamic.get(key).result().isPresent) {
                var statDynamic = baseDynamic.get(key).get().orThrow
                statDynamic = statDynamic.updateMapValues { Pair.of(this.getStatIdFromLiteral(it.first), it.second) }
                baseDynamic = baseDynamic.set(key, statDynamic)
            }
        }
        return baseDynamic
    }

    private fun getStatIdFromLiteral(key: Dynamic<*>): Dynamic<*> {
        return DataFixUtils.orElse(key.asString().result().map { literal ->
            this.stringToStat[literal]?.let { key.createString(it.toString()) }
        }, key)
    }

}