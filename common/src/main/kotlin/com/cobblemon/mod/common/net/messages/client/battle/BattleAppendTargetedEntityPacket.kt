package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class BattleAppendTargetedEntityPacket(val sourceEntityId: Int, val targetedEntityId: Int) : NetworkPacket<BattleAppendTargetedEntityPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeVarInt(sourceEntityId)
        buffer.writeVarInt(targetedEntityId)
    }

    companion object {
        val ID = cobblemonResource("battle_append_targeted")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleAppendTargetedEntityPacket(buffer.readVarInt(), buffer.readVarInt())
    }

}