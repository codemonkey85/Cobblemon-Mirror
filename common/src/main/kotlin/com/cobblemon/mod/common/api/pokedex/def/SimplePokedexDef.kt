/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.def

import com.cobblemon.mod.common.api.pokedex.entry.DexEntries
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeSizedInt
import com.google.common.collect.Lists
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

/**
 * A [PokedexDef] that is just a list of [PokedexEntry]s
 *
 * @since August 24, 2024
 * @author Apion
 */
class SimplePokedexDef(
    override val id: ResourceLocation
) : PokedexDef() {
    override val typeId = ID

    private val entries = mutableListOf<ResourceLocation>()

    fun appendEntries(entries: List<ResourceLocation>) {
        this.entries.addAll(entries)
    }

    override fun getEntries() = entries.mapNotNull { DexEntries.entries[it] }

    override fun shouldSynchronize(other: PokedexDef) = true

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        sortOrder = buffer.readSizedInt(IntSize.U_BYTE)
        val size = buffer.readInt()
        for (i in 0 until size) {
            entries.add(buffer.readIdentifier())
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, sortOrder)
        buffer.writeInt(entries.size)
        entries.forEach {
            buffer.writeIdentifier(it)
        }
    }

    companion object {
        val ID = cobblemonResource("simple_pokedex_def")
        val CODEC: MapCodec<SimplePokedexDef> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter { it.id },
                PrimitiveCodec.INT.fieldOf("sortOrder").forGetter { it.sortOrder },
                ResourceLocation.CODEC.listOf().fieldOf("entries").forGetter { it.entries }
            ).apply(instance) { id, sortOrder, entries ->
                val result = SimplePokedexDef(id)
                result.sortOrder = sortOrder
                result.entries.addAll(entries)
                result
            }
        }

        val PACKET_CODEC: StreamCodec<ByteBuf, SimplePokedexDef> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SimplePokedexDef::id,
            ByteBufCodecs.INT, SimplePokedexDef::sortOrder,
            ByteBufCodecs.collection(Lists::newArrayListWithCapacity, ResourceLocation.STREAM_CODEC), SimplePokedexDef::entries
        ) { id, sortOrder, entries ->
            val result = SimplePokedexDef(id)
            result.sortOrder = sortOrder
            result.entries.addAll(entries)
            result
        }
    }
}