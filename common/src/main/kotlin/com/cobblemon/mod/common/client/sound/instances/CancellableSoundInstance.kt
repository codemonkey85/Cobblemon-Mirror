/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.sound.instances

import com.cobblemon.mod.common.client.sound.BlockEntitySoundTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.resources.sounds.TickableSoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

class CancellableSoundInstance(sound: SoundEvent, pos: BlockPos = BlockPos.ZERO, repeat: Boolean = false, volume: Float = 1.0F, pitch: Float = 1.0F) :
    SimpleSoundInstance(sound, SoundSource.BLOCKS, volume, pitch, SoundInstance.createUnseededRandom(), pos),
    TickableSoundInstance {

    private val soundManager = Minecraft.getInstance().soundManager;
    private var done: Boolean = false
    private var unheardTicks = 0
    private var initVolume = 1.0
    var pos : BlockPos

    init {
        this.relative = false
        this.looping = repeat
        this.attenuation = SoundInstance.Attenuation.NONE
        this.initVolume = volume.toDouble()
        this.pos = pos
        this.attenuation = SoundInstance.Attenuation.LINEAR
    }

    override fun isStopped(): Boolean {
        return this.done
    }

    override fun tick() {
        if(soundManager.isActive(this) && !this.looping) {
            this.done = true
            BlockEntitySoundTracker.stop(this.pos, this)
        } else {
            // Using the player's position as a proxy for a SoundListener.
            val listenerPos = Minecraft.getInstance().player?.position()?.distanceToSqr(
                Vec3(
                    this.x,
                    this.y,
                    this.z
                )
            )
            if(listenerPos != null) {
                if(listenerPos > ATTENUATION_DISTANCE_MAX_SQUARED * 2) {
                    // listener is very far away, kill it
                    this.done = true
                    BlockEntitySoundTracker.stop(this.pos, this)
                } else if( listenerPos > ATTENUATION_DISTANCE_MAX_SQUARED) {
                    ++unheardTicks
                    // Purpose of this is if a client is pushing in and out of the attenuation range, they won't notice the sound abruptly stopping
                    if(unheardTicks > UNHEARD_TICKS_MAX) {
                        this.done = true
                        this.looping = false
                        BlockEntitySoundTracker.stop(this.pos, this)
                    }
                }
            } else {
                unheardTicks = 0
            }
        }
    }

    companion object {
        const val UNHEARD_TICKS_MAX = 200
        const val ATTENUATION_DISTANCE_MAX_SQUARED = 18 * 18
    }
}
