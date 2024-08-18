package com.cobblemon.mod.common.api.dex

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.dex.entry.DexEntry
import com.cobblemon.mod.common.api.dex.entry.ExtraDexData
import com.cobblemon.mod.common.api.dex.entry.ExtraDexDataTypes
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * A list of dex entries
 */
class DexDef: ClientDataSynchronizer<DexDef> {
    val id = cobblemonResource("blank")
    val entries = mutableListOf<ResourceLocation>()
    override fun shouldSynchronize(other: DexDef) = true

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        val size = buffer.readInt()
        for (i in 0 until size) {
           entries.add(buffer.readIdentifier())
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entries.size)
        entries.forEach {
            buffer.writeIdentifier(it)
        }
    }


}