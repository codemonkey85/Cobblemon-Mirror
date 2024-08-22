package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * A list of dex entries
 */
class PokedexDef: ClientDataSynchronizer<PokedexDef> {
    val id = cobblemonResource("blank")
    val entries = mutableListOf<ResourceLocation>()
    override fun shouldSynchronize(other: PokedexDef) = true

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