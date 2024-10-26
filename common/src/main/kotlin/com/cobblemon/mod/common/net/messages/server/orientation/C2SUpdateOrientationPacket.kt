package com.cobblemon.mod.common.net.messages.server.orientation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readMatrix3f
import com.cobblemon.mod.common.util.writeMatrix3f
import net.minecraft.network.RegistryFriendlyByteBuf
import org.joml.Matrix3f

class C2SUpdateOrientationPacket internal constructor(val orientation: Matrix3f?) : NetworkPacket<C2SUpdateOrientationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(orientation != null)
        if (orientation != null) {
            buffer.writeMatrix3f(orientation)
        }
    }

    companion object {
        val ID = cobblemonResource("c2s_update_orientation")
        fun decode(buffer: RegistryFriendlyByteBuf): C2SUpdateOrientationPacket {
            val orientation = if (buffer.readBoolean()) buffer.readMatrix3f() else null
            return C2SUpdateOrientationPacket(orientation)
        }
    }
}
