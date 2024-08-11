package com.cobblemon.mod.common.net.messages.server.pokedex.scanner

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.MapUpdatePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class StartScanningPacket(val targetedId: Int) : NetworkPacket<StartScanningPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(targetedId)
    }

    companion object {
        val ID = cobblemonResource("start_scanning_packet")

        fun decode(buffer: RegistryFriendlyByteBuf): StartScanningPacket {
            val targetId = buffer.readInt()
            return StartScanningPacket(targetId)
        }
    }
}