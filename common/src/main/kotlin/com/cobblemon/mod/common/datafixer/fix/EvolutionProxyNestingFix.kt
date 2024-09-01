/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.util.DataKeys
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic

class EvolutionProxyNestingFix(outputSchema: Schema) : PokemonFix(outputSchema) {

    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        if (dynamic.get(DataKeys.POKEMON_EVOLUTIONS).result().isPresent) {
            val evolutionsDynamic = dynamic.get(DataKeys.POKEMON_EVOLUTIONS).get().orThrow
            if (evolutionsDynamic.get(POKEMON_PENDING_EVOLUTIONS).result().isPresent) {
                val pendingDynamic = evolutionsDynamic.get(POKEMON_PENDING_EVOLUTIONS).get().orThrow
                return dynamic.set(DataKeys.POKEMON_EVOLUTIONS, pendingDynamic)
            }
        }
        return dynamic
    }

    companion object {
        private const val POKEMON_PENDING_EVOLUTIONS = "Pending"
    }

}