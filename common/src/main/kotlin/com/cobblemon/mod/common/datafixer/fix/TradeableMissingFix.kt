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

class TradeableMissingFix(outputSchema: Schema) : PokemonFix(outputSchema) {
    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        if (dynamic.get(DataKeys.POKEMON_TRADEABLE).result().isPresent) {
            return dynamic
        }
        return dynamic.set(DataKeys.POKEMON_TRADEABLE, dynamic.createBoolean(true))
    }

}