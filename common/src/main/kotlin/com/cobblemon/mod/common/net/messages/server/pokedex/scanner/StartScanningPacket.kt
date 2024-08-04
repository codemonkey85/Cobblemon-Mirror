package com.cobblemon.mod.common.net.messages.server.pokedex.scanner

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.MapUpdatePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class StartScanningPacket(val targetedUuid: UUID) : NetworkPacket<StartScanningPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(targetedUuid)
    }

    companion object {
        val ID = cobblemonResource("start_scanning__packet")

        fun decode(buffer: RegistryFriendlyByteBuf): StartScanningPacket {
            val targetUuid = buffer.readUUID()
            return StartScanningPacket(targetUuid)
        }
    }
}