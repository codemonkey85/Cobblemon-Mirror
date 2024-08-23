/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.entry

import com.bedrockk.molang.MoLang
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ListExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class BasicPokedexVariation(
    val langKey: String = "cobblemon.ui.pokedex.info.form.normal",
    val aspects: String = "",
    val conditions: List<ExpressionLike> = emptyList(),
) : PokedexVariation() {
    override val type = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(langKey)
        buffer.writeString(aspects)
        buffer.writeInt(conditions.size)
        conditions.forEach {
            buffer.writeString(it.toString())
        }
    }

    companion object {
        val ID = cobblemonResource("basic_pokedex_variation")

        fun decode(buffer: RegistryFriendlyByteBuf): BasicPokedexVariation {
            val langKey = buffer.readString()
            val aspectString = buffer.readString()
            val numToRead = buffer.readInt()
            val conditions = mutableListOf<ExpressionLike>()
            for (i in 0 until numToRead) {
                val expressions = MoLang.createParser(buffer.readString()).parse()
                conditions.add(ListExpression(expressions))
            }
            return BasicPokedexVariation(langKey, aspectString, conditions)
        }
    }
}