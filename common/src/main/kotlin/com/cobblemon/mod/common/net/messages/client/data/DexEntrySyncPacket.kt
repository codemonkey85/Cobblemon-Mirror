package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.dex.DexDef
import com.cobblemon.mod.common.api.dex.entry.DexEntries
import com.cobblemon.mod.common.api.dex.entry.DexEntry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class DexEntrySyncPacket(dexEntries: Collection<DexEntry>) : DataRegistrySyncPacket<DexEntry, DexDefSyncPacket>(dexEntries) {
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: DexEntry) {
        entry.encode(buffer)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): DexEntry? {
        return DexEntry.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<DexEntry>) {
        DexEntries.reload(entries.associateBy { it.entryId })
    }

    override val id = ID

    companion object {
        val ID = cobblemonResource("dex_entry_sync")
    }
}