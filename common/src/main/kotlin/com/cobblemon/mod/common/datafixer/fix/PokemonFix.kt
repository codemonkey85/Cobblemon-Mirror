/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.datafixer.CobblemonTypeReferences
import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFix
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import java.util.function.Function

abstract class PokemonFix(outputSchema: Schema) : DataFix(outputSchema, false) {
    override fun makeRule(): TypeRewriteRule {
        val type = DSL.named(CobblemonTypeReferences.POKEMON.typeName(), DSL.remainderType())
        val schemaType = this.inputSchema.getType(CobblemonTypeReferences.POKEMON)
        require(type == schemaType)
        { "${CobblemonTypeReferences.POKEMON.typeName()} is not what was expected" }
        return this.fixTypeEverywhere(this::class.simpleName, type) {
            Function { pair -> pair.mapSecond(this::fixPokemonData) }
        }
    }

    protected abstract fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*>
}