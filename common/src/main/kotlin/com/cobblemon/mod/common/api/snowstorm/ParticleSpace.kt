/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Settings around the space in which the particle functions.
 *
 * @author Hiroku
 * @since February 13th, 2023
 */
class ParticleSpace(
    var localPosition: Boolean = false,
    var localRotation: Boolean = false,
    var localVelocity: Boolean = false
) {
    companion object {
        val CODEC: Codec<ParticleSpace> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.BOOL.fieldOf("localPosition").forGetter { it.localPosition },
                PrimitiveCodec.BOOL.fieldOf("localRotation").forGetter { it.localRotation },
                PrimitiveCodec.BOOL.fieldOf("localVelocity").forGetter { it.localVelocity }
            ).apply(instance, ::ParticleSpace)
        }
    }

    val isLocalSpace: Boolean
        get() = localPosition || localRotation

    fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        localPosition = buffer.readBoolean()
        localRotation = buffer.readBoolean()
        localVelocity = buffer.readBoolean()
    }

    fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(localPosition)
        buffer.writeBoolean(localRotation)
        buffer.writeBoolean(localVelocity)
    }
}