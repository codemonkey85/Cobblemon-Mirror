/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType
import com.cobblemon.mod.common.entity.npc.NPCPlayerTexture
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.writeEnumConstant
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer

object NPCPlayerTextureSerializer : EntityDataSerializer<NPCPlayerTexture> {
    val ID = cobblemonResource("npcplayertexture")

    fun write(buffer: RegistryFriendlyByteBuf, texture: NPCPlayerTexture) {
        buffer.writeEnumConstant(texture.model)
        if (texture.model == NPCPlayerModelType.NONE) {
            return
        }
        buffer.writeByteArray(texture.texture)
    }

    fun read(buffer: RegistryFriendlyByteBuf): NPCPlayerTexture {
        val model = buffer.readEnumConstant(NPCPlayerModelType::class.java)
        val texture = if (model == NPCPlayerModelType.NONE) ByteArray(1) else buffer.readByteArray()
        return NPCPlayerTexture(texture, model)
    }

    override fun codec() = StreamCodec.of(::write, ::read)
    override fun copy(texture: NPCPlayerTexture) = NPCPlayerTexture(texture.texture, texture.model)
}