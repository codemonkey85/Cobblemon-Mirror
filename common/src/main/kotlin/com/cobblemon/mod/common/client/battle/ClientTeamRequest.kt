package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.client.requests.ClientPlayerActionRequest
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import java.util.*

data class ClientTeamRequest(
    override val requestID: UUID,
    override val expiryTime: Int
) : ClientPlayerActionRequest, ClientPlayerIcon(expiryTime) {
    override val texture: ResourceLocation
        get() = cobblemonResource("textures/particle/icon_team.png")
}