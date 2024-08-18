package com.cobblemon.mod.common.api.dex.entry

import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class DexEntry(
    val id: ResourceLocation,
    val registryId: ResourceLocation,
    val entryId: ResourceLocation,
    val extraData: List<ExtraDexData>
) {
    fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeIdentifier(id)
        buf.writeIdentifier(registryId)
        buf.writeIdentifier(entryId)
        buf.writeInt(extraData.size)
        extraData.forEach {
            it.encode(buf)
        }
    }
    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): DexEntry {
            val id = buffer.readIdentifier()
            val registryId = buffer.readIdentifier()
            val entryId = buffer.readIdentifier()
            val numEntries = buffer.readInt()
            val entries = mutableListOf<ExtraDexData>()
            for (i in 0 until numEntries) {
                entries.add(ExtraDexData.decodeAll(buffer))
            }
            return DexEntry(id, registryId, entryId, entries)
        }
    }
}