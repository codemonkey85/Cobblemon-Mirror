package com.cobblemon.mod.common.api.pokedex.def

import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import com.google.common.collect.Lists
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

class AggregatePokedexDef(
    override val id: ResourceLocation
) : PokedexDef() {
    override val typeId = ID
    private val subDexIds = mutableListOf<ResourceLocation>()

    override fun getEntries(): List<PokedexEntry> {
        return subDexIds.mapNotNull { Dexes.dexEntryMap[it] }
            .flatMap { it.getEntries() }
    }

    override fun shouldSynchronize(other: PokedexDef) = true

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        val size = buffer.readInt()
        for (i in 0 until size) {
            subDexIds.add(buffer.readIdentifier())
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(subDexIds.size)
        subDexIds.forEach {
            buffer.writeIdentifier(it)
        }
    }

    companion object {
        val ID = cobblemonResource("aggregate_pokedex_def")
        val CODEC: MapCodec<AggregatePokedexDef> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter {it.id},
                ResourceLocation.CODEC.listOf().fieldOf("subDexIds").forGetter { it.subDexIds }
            ).apply(instance) {id, entries ->
                val result = AggregatePokedexDef(id)
                result.subDexIds.addAll(entries)
                result
            }
        }
        val PACKET_CODEC: StreamCodec<ByteBuf, AggregatePokedexDef> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AggregatePokedexDef::id,
            ByteBufCodecs.collection(Lists::newArrayListWithCapacity, ResourceLocation.STREAM_CODEC), AggregatePokedexDef::subDexIds
        ) { id, entries ->
            val result = AggregatePokedexDef(id)
            //result.subDexIds.addAll(entries)
            result
        }
    }
}