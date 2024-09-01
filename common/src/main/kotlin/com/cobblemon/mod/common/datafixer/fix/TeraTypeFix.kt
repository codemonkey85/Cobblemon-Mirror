/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.api.types.tera.gimmick.StellarTeraType
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic

class TeraTypeFix(outputSchema: Schema) : PokemonFix(outputSchema) {

    // Don't access the regular types directly.
    private val literalToIdentifier = mapOf(
        "normal" to cobblemonResource("normal"),
        "fire" to cobblemonResource("fire"),
        "water" to cobblemonResource("water"),
        "grass" to cobblemonResource("grass"),
        "electric" to cobblemonResource("electric"),
        "ice" to cobblemonResource("ice"),
        "fighting" to cobblemonResource("fighting"),
        "poison" to cobblemonResource("poison"),
        "ground" to cobblemonResource("ground"),
        "flying" to cobblemonResource("flying"),
        "psychic" to cobblemonResource("psychic"),
        "bug" to cobblemonResource("bug"),
        "rock" to cobblemonResource("rock"),
        "ghost" to cobblemonResource("ghost"),
        "dragon" to cobblemonResource("dragon"),
        "dark" to cobblemonResource("dark"),
        "steel" to cobblemonResource("steel"),
        "fairy" to cobblemonResource("fairy"),
        StellarTeraType.ID.path to StellarTeraType.ID
    )

    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        return dynamic.update(DataKeys.POKEMON_TERA_TYPE) { literalDynamic ->
            literalDynamic.asString()
                .map { literal -> this.literalToIdentifier[literal]?.toString() ?: literal }
                .mapOrElse({ literalDynamic.createString(it) }, { literalDynamic })
        }
    }

}