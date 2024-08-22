package com.cobblemon.mod.common.api.storage.player.client

import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class ClientPokedexManager(
    override val entries: MutableMap<String, String>,
    override val isIncrement: Boolean = false
) : AbstractPokedexManager(), ClientInstancedPlayerData {
    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeInt(entries.size)
        for (entry in entries) {
            buf.writeString(entry.key)
            buf.writeString(entry.value)
        }
    }

    companion object {
        fun decode(buf: RegistryFriendlyByteBuf): SetClientPlayerDataPacket {
            val map = mutableMapOf<String, String>()
            val numEntries = buf.readInt()
            for (i in 0 until numEntries) {
                val key = buf.readString()
                val value = buf.readString()
                map[key] = value
            }
            return SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, ClientPokedexManager(map, false))
        }

        fun runAction(data: ClientInstancedPlayerData) {
            if (data !is ClientPokedexManager) return
            CobblemonClient.clientPokedexData = data
        }
    }
}