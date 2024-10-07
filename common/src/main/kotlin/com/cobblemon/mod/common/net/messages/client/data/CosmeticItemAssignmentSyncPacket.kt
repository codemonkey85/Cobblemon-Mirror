/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.CobblemonCosmeticItems
import com.cobblemon.mod.common.pokemon.cosmetic.CosmeticItemAssignment
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class CosmeticItemAssignmentSyncPacket(
    assignments: Collection<CosmeticItemAssignment>
) : DataRegistrySyncPacket<CosmeticItemAssignment, CosmeticItemAssignmentSyncPacket>(assignments) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: CosmeticItemAssignment) {
        CosmeticItemAssignment.PACKET_CODEC.encode(buffer, entry)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): CosmeticItemAssignment? {
        return CosmeticItemAssignment.PACKET_CODEC.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<CosmeticItemAssignment>) {
        CobblemonCosmeticItems.reload(entries.associateBy { it.id })
    }

    companion object {
        val ID = cobblemonResource("cosmetic_item_assignment_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = CosmeticItemAssignmentSyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}