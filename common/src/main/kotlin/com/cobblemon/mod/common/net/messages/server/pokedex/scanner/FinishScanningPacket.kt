package com.cobblemon.mod.common.net.messages.server.pokedex.scanner

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.MapUpdatePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class FinishScanningPacket(val targetedId: Int) : NetworkPacket<FinishScanningPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(targetedId)
    }

    companion object {
        val ID = cobblemonResource("finish_scanning_packet")

        fun decode(buffer: RegistryFriendlyByteBuf): FinishScanningPacket {
            val targetId = buffer.readInt()
            return FinishScanningPacket(targetId)
        }
    }
}