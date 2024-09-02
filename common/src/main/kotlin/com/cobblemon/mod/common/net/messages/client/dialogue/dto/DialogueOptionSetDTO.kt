/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.dialogue.dto

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import net.minecraft.network.RegistryFriendlyByteBuf

class DialogueOptionSetDTO(
    var deadline: Float = 0F,
    var options: List<DialogueOptionDTO> = emptyList()
): Encodable, Decodable {
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeFloat(deadline)
        buffer.writeInt(options.size)
        options.forEach { option ->
            option.encode(buffer)
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        deadline = buffer.readFloat()
        val size = buffer.readInt()
        options = (0 until size).map { DialogueOptionDTO().apply { decode(buffer) } }
    }
}