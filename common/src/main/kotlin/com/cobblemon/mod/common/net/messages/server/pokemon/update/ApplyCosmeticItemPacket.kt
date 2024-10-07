/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

class ApplyCosmeticItemPacket(
    val pokemonId: UUID,
    val isParty: Boolean,
    val cosmeticItemId: UUID,
    /* The index of the cosmetic item being applied (from inside [CosmeticItemAssignment.cosmeticItems]*/
    val cosmeticItemIndex: Int
) : NetworkPacket<ApplyCosmeticItemPacket> {
    companion object {
        val ID = cobblemonResource("apply_cosmetic_item")
        fun decode(buffer: RegistryFriendlyByteBuf) = ApplyCosmeticItemPacket(
            buffer.readUUID(), buffer.readBoolean(), buffer.readUUID(), buffer.readSizedInt(IntSize.U_SHORT)
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonId)
        buffer.writeBoolean(isParty)
        buffer.writeUUID(cosmeticItemId)
        buffer.writeSizedInt(IntSize.U_SHORT, cosmeticItemIndex)
    }
}