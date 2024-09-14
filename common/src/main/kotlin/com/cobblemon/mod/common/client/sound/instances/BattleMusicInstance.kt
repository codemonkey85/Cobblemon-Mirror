/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.sound.instances

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.client.sound.BattleMusicController
import com.cobblemon.mod.common.util.resumeSounds
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.resources.sounds.TickableSoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.Mth

/**
 * A [SoundInstance] for [PokemonBattle] music.
 *
 * While an instance is being played, the [MusicTracker] will be frozen and the [SoundManager] will not play
 * [SoundSource]s belonging to [BattleMusicController.filteredCategories].
 *
 * @author Segfault Guy
 * @since April 22nd, 2023
 */
class BattleMusicInstance(sound: SoundEvent, volume: Float = 1.0F, pitch: Float = 1.0F) :
    SimpleSoundInstance(sound, SoundSource.MUSIC, volume, pitch, SoundInstance.createUnseededRandom(), 0.0, 0.0, 0.0), TickableSoundInstance {

    private val soundManager = Minecraft.getInstance().soundManager;
    private var fade: Boolean = false
    private var done: Boolean = false
    private var tickCount = 0
    private var fadeCount = 0
    private val fadeTime = 60.0
    private var initVolume = 1.0

    init {
        this.relative = true
        this.looping = true
        this.attenuation = SoundInstance.Attenuation.NONE
        this.initVolume = volume.toDouble()
    }

    override fun isStopped(): Boolean {
        if (this.done) BattleMusicController.filteredCategories.forEach { soundManager.resumeSounds(null, it) }
        return this.done
    }

    /** Flags to fade and end this instance. */
    fun setFade() {
        this.fade = true
        this.looping = false
    }

    override fun tick() {
        ++tickCount
        if (fade) {
            ++fadeCount
            this.volume = Mth.lerp(fadeCount/fadeTime, initVolume, 0.0).toFloat()
            if (volume <= 0) this.done = true
        }
    }
}
