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
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeText
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent

class DialogueOptionDTO(
    var text: MutableComponent = "".text(),
    var value: String = "",
    var selectable: Boolean = true
): Encodable, Decodable {
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeText(text)
        buffer.writeString(value)
        buffer.writeBoolean(selectable)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        text = buffer.readText().copy()
        value = buffer.readString()
        selectable = buffer.readBoolean()
    }
}