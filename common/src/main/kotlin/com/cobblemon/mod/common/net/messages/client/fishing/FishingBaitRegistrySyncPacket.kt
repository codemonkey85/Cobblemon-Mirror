/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.fishing

import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.net.messages.client.data.DataRegistrySyncPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

class FishingBaitRegistrySyncPacket(fishingBaits: List<FishingBait>) : DataRegistrySyncPacket<FishingBait, FishingBaitRegistrySyncPacket>(fishingBaits) {
    companion object {
        val ID = cobblemonResource("fishing_baits")
        fun decode(buffer: RegistryFriendlyByteBuf) = FishingBaitRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

    override val id = ID
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: FishingBait) {
        buffer.writeIdentifier(entry.item)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): FishingBait {
        // server handles the effects so client doesnt need to know them
        return FishingBait(buffer.readIdentifier(), emptyList())
    }

    override fun synchronizeDecoded(entries: Collection<FishingBait>) {
        FishingBaits.reload(entries.associateBy { it.item })
    }
}