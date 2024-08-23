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
import com.cobblemon.mod.common.api.molang.SingleExpression
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.FriendlyByteBuf.readNullable
import net.minecraft.network.FriendlyByteBuf.writeNullable
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class PokedexEntry(
    val id: ResourceLocation,
    val entryId: ResourceLocation,
    val displayEntry: Int,
    val displayConditions: List<ExpressionLike>?,
    val variations: List<PokedexVariation>
) {
    fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeIdentifier(id)
        buf.writeIdentifier(entryId)
        buf.writeInt(displayEntry)
        buf.writeNullable(displayConditions) {buffer, conditions ->
            buffer.writeInt(conditions.size)
            conditions.forEach {
                buffer.writeString(it.getString())
            }
        }
        buf.writeInt(variations.size)
        variations.forEach {
            it.encode(buf)
        }
    }
    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): PokedexEntry {
            val id = buffer.readIdentifier()
            val entryId = buffer.readIdentifier()
            val displayEntry = buffer.readInt()
            val conditions = readNullable(buffer) {buf ->
                val numConditions = buffer.readInt()
                (0 until numConditions).map {
                    SingleExpression(MoLang.createParser(buffer.readString()).parseExpression())
                }
            }
            val variationsSize = buffer.readInt()
            val entries = mutableListOf<PokedexVariation>()
            for (i in 0 until variationsSize) {
                entries.add(PokedexVariation.decodeAll(buffer))
            }
            return PokedexEntry(id, entryId, displayEntry, conditions, entries)
        }
    }
}