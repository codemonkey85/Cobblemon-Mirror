/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import java.util.UUID

class ClientPlayerTeamData {
    var multiBattleTeamMembers = mutableListOf<ClientMultiBattleTeamMember>()
}

class ClientMultiBattleTeamMember(val uuid: UUID, val name: MutableComponent) : ClientPlayerIcon(null) {
    override val texture: ResourceLocation = cobblemonResource("textures/particle/request/icon_partner.png")
}