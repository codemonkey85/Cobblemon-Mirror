package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class PokedexEntry(
    val id: ResourceLocation,
    val entryId: ResourceLocation,
    val displayEntry: Int,
    val variations: List<PokedexVariation>
) {
    fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeIdentifier(id)
        buf.writeIdentifier(entryId)
        buf.writeInt(displayEntry)
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
            val variationsSize = buffer.readInt()
            val entries = mutableListOf<PokedexVariation>()
            for (i in 0 until variationsSize) {
                entries.add(PokedexVariation.decodeAll(buffer))
            }
            return PokedexEntry(id, entryId, displayEntry, entries)
        }
    }
}