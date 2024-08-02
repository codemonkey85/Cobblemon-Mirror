/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.sounds.SoundEvent

/**
 * Instructs a client what SoundEvent to play during a battle. If the SoundEvent specified is null, the SoundEvent
 * currently playing will terminate gracefully.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleMusicHandler].
 *
 * @author Segfault Guy
 * @since April 20th, 2023
 */
class BattleMusicPacket(var music : SoundEvent? = null, var volume: Float = 1.0f, var pitch: Float = 1.0f) : NetworkPacket<BattleMusicPacket> {
    companion object {
        val ID = cobblemonResource("battle_music")
        fun decode(buffer: RegistryFriendlyByteBuf) =  BattleMusicPacket(
            music = BuiltInRegistries.SOUND_EVENT.get(buffer.readIdentifier()),
            volume = buffer.readFloat(),
            pitch = buffer.readFloat()
        )
    }

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        music?.let { buffer.writeIdentifier(it.location) } ?: buffer.writeIdentifier("".asIdentifierDefaultingNamespace())
        buffer.writeFloat(volume)
        buffer.writeFloat(pitch)
    }
}