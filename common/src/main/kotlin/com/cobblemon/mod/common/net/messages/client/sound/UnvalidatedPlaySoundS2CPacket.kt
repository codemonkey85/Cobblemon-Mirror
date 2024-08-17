/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.sound

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.sounds.SoundSource
import net.minecraft.resources.ResourceLocation

/**
 * A class meant to mimic [PlaySoundS2CPacket] without validating the Sound Event registry.
 * This should only be used for our dynamic sounds such as Pokémon ambience.
 *
 * @author Licious
 * @since December 29th, 2022
 */
internal class UnvalidatedPlaySoundS2CPacket(
    val sound: ResourceLocation,
    val category: SoundSource,
    val x: Double,
    val y: Double,
    val z: Double,
    val volume: Float,
    val pitch: Float
) : NetworkPacket<UnvalidatedPlaySoundS2CPacket> {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(this.sound)
        buffer.writeEnumConstant(this.category)
        buffer.writeDouble(this.x)
        buffer.writeDouble(this.y)
        buffer.writeDouble(this.z)
        buffer.writeFloat(this.volume)
        buffer.writeFloat(this.pitch)
    }

    companion object {
        val ID = cobblemonResource("unvalidated_play_sound")
        fun decode(buffer: RegistryFriendlyByteBuf) = UnvalidatedPlaySoundS2CPacket(
            buffer.readIdentifier(),
            buffer.readEnumConstant(SoundSource::class.java),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readFloat(),
            buffer.readFloat()
        )
    }
}