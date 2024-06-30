package com.cobblemon.mod.common.net.messages.client.ui

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readItemStack
import com.cobblemon.mod.common.util.writeItemStack
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf

/**
 * Tells the server to attempt crafting a Blank TM using the [TMBlock]
 *
 * Handled by [CraftBlankTMPacketHandler]
 *
 * @author whatsy
 */
class CraftBlankTMPacket(
    val ingredient: ItemStack
): NetworkPacket<CraftBlankTMPacket> {
    override val id = ID

    override fun encode(buffer: RegistryByteBuf) {
        buffer.writeItemStack(ingredient)
    }

    companion object {
        val ID = cobblemonResource("craft_blank_tm")

        fun decode(buffer: RegistryByteBuf) = CraftBlankTMPacket(
            buffer.readItemStack()
        )
    }

}