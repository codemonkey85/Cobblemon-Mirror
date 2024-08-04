package com.cobblemon.mod.common.net.messages.server.pokedex

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.util.Identifier

class MapUpdatePacket(val imageBytes: ByteArray) : NetworkPacket<MapUpdatePacket> {
    override val id = ID

    override fun encode(buffer: RegistryByteBuf) {
        buffer.writeByteArray(imageBytes)
    }

    companion object {
        val ID = cobblemonResource("update_map_packet")

        fun decode(buffer: RegistryFriendlyByteBuf): MapUpdatePacket {
            val imageBytes = buffer.readByteArray()
            return MapUpdatePacket(imageBytes)
        }
    }
}
