/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.effect

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to run some MoLang in the MoLang environment associated with the given
 * entity (which we're assuming is a posable entity).
 *
 * The expressions are given as strings instead of Expressions because if we make it
 * Expressions and you constructed it from code instead of string parsing then it won't
 * encode-decode properly, and you'll report a bug. Reason why that is: only way to
 * serialize an Expression is to use the originalString property it was parsed from,
 * so if you didn't parse from string that'll be blank and meaningless.
 *
 * Handled by [com.cobblemon.mod.common.client.net.effect.RunPosableMoLangHandler].
 *
 * @author Hiroku
 * @since October 27th, 2023
 */
class RunPosableMoLangPacket(
    val entityId: Int,
    val expressions: Set<String>
) : NetworkPacket<RunPosableMoLangPacket> {
    companion object {
        val ID = cobblemonResource("run_posable_molang")
        fun decode(buffer: RegistryFriendlyByteBuf) = RunPosableMoLangPacket(
            buffer.readInt(),
            buffer.readList { pb -> pb.readString() }.toSet()
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeCollection(expressions) { pb, value -> pb.writeString(value) }
    }
}