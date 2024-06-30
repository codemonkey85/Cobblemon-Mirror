/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.toast

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryByteBuf
import java.util.UUID
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ToastPacket(
    val title: Text,
    val description: Text,
    val icon: ItemStack,
    val frameTexture: Identifier,
    val progress: Float,
    val progressColor: Int,
    val uuid: UUID,
    val behaviour: Behaviour
) : NetworkPacket<ToastPacket> {

    override val id: Identifier = ID

    override fun encode(buffer: RegistryByteBuf) {
        buffer.writeText(this.title)
        buffer.writeText(this.description)
        buffer.writeItemStack(this.icon)
        buffer.writeIdentifier(this.frameTexture)
        buffer.writeFloat(this.progress)
        buffer.writeInt(this.progressColor)
        buffer.writeUuid(this.uuid)
        buffer.writeEnumConstant(this.behaviour)
    }

    companion object {

        val ID = cobblemonResource("toast")

        fun decode(buffer: RegistryByteBuf): ToastPacket = ToastPacket(
            buffer.readText(),
            buffer.readText(),
            buffer.readItemStack(),
            buffer.readIdentifier(),
            buffer.readFloat(),
            buffer.readInt(),
            buffer.readUuid(),
            buffer.readEnumConstant(Behaviour::class.java)
        )

    }

    enum class Behaviour {
        SHOW_OR_UPDATE,
        HIDE
    }

}