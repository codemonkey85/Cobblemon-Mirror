package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.pokedex.entry.DexEntries
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class DexEntrySyncPacket(dexEntries: Collection<PokedexEntry>) : DataRegistrySyncPacket<PokedexEntry, DexDefSyncPacket>(dexEntries) {
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: PokedexEntry) {
        entry.encode(buffer)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): PokedexEntry? {
        return PokedexEntry.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<PokedexEntry>) {
        DexEntries.reload(entries.associateBy { it.entryId })
    }

    override val id = ID

    companion object {
        val ID = cobblemonResource("dex_entry_sync")
    }
}