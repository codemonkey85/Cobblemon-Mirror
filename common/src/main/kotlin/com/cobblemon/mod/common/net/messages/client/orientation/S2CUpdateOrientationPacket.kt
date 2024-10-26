package com.cobblemon.mod.common.net.messages.client.orientation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import org.joml.Matrix3f

class S2CUpdateOrientationPacket internal constructor(val entityId: Int, val orientation: Matrix3f?) : NetworkPacket<S2CUpdateOrientationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeBoolean(orientation != null)
        if (orientation != null) {
            buffer.writeMatrix3f(orientation)
        }
    }

    companion object {
        val ID = cobblemonResource("s2c_update_orientation")
        fun decode(buffer: RegistryFriendlyByteBuf): S2CUpdateOrientationPacket {
            val entityId = buffer.readInt()
            val orientation = if (buffer.readBoolean()) buffer.readMatrix3f() else null
            return S2CUpdateOrientationPacket(entityId, orientation)
        }
    }
}
