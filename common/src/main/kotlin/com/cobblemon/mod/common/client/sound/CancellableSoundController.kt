/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.sound

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.BlockPos

object CancellableSoundController {

    private val manager = Minecraft.getInstance().soundManager
    private val playingSounds: MutableMap<BlockPos, MutableMap<ResourceLocation, SoundInstance>> = emptyMap<BlockPos, MutableMap<ResourceLocation, SoundInstance>>().toMutableMap()

    /** Start a new  and pause all [filteredCategories] sounds currently playing. */
    fun playSound(newSound: CancellableSoundInstance) {
        manager.play(newSound)

        var idMap = playingSounds[newSound.pos]
        var soundInstance : SoundInstance? = null
        if(idMap == null) {
            idMap = emptyMap<ResourceLocation, SoundInstance>().toMutableMap()
        } else {
            soundInstance = idMap[newSound.location]
        }
        if(soundInstance != null) {
            manager.stop(soundInstance)
        }
        idMap[newSound.location] = newSound
        playingSounds[newSound.pos] = idMap
    }

    fun stopSound(soundInstance: CancellableSoundInstance) {
        this.stopSound(soundInstance.pos, soundInstance.sound.location)//import com.cobblemon.mod.common.client.sound.com.cobblemon.mod.common.client.sound.CancellableSoundInstance

    }
    fun stopSound(blockPos: BlockPos, identifier: ResourceLocation) {
        val idMap = playingSounds.get(blockPos)
        if(idMap != null) {
            val soundInstance = idMap.get(identifier)
            if(soundInstance !== null) {
                manager.stop(soundInstance)
                idMap.remove(identifier)
                if(idMap.keys.size == 0) {
                    playingSounds.remove(blockPos)
                }
            }
        }
    }
}