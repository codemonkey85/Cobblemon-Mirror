package com.cobblemon.mod.common.client.requests

import com.cobblemon.mod.common.api.interaction.PlayerActionRequest
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.MutableComponent

/**
 * An inbound [PlayerActionRequest].
 *
 * @author Segfault Guy
 * @since September 21st, 2024
 */
abstract class ClientPlayerActionRequest(expiryTime: Int) : ClientPlayerIcon(expiryTime), PlayerActionRequest {

    companion object {
        /** Client message to inform the player about a [langKey] request from [senderName]. */
        fun notify(langKey: String, senderName: MutableComponent, vararg params: Any) {
            val lang = lang(langKey, senderName.aqua(), *params).yellow()
            Minecraft.getInstance().player!!.displayClientMessage(lang, false)
        }
    }
}