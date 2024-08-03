/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.sound.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.util.resumeSounds
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.*
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

/**
 * A [SoundInstance] for [PokemonBattle] music.
 *
 * While an instance is being played, the [MusicTracker] will be frozen and the [SoundManager] will not play
 * [SoundCategory]s belonging to [BattleMusicController.filteredCategories].
 *
 * @author Segfault Guy
 * @since April 22nd, 2023
 */
class BattleMusicInstance(sound: SoundEvent, volume: Float = 1.0F, pitch: Float = 1.0F) :
        PositionedSoundInstance(sound, SoundCategory.MUSIC, volume, pitch, SoundInstance.createRandom(), 0.0, 0.0, 0.0), TickableSoundInstance {

    private val soundManager = MinecraftClient.getInstance().soundManager;
    private var fade: Boolean = false
    private var done: Boolean = false
    private var tickCount = 0
    private var fadeCount = 0
    private val fadeTime = 60.0
    private var initVolume = 1.0

    init {
        this.relative = true
        this.repeat = true
        this.attenuationType = SoundInstance.AttenuationType.NONE
        this.initVolume = volume.toDouble()
    }

    override fun isDone(): Boolean {
        if (this.done) BattleMusicController.filteredCategories.forEach { soundManager.resumeSounds(null, it) }
        return this.done
    }

    /** Flags to fade and end this instance. */
    fun setFade() {
        this.fade = true
        this.repeat = false
    }

    override fun tick() {
        ++tickCount
        if (fade) {
            ++fadeCount
            this.volume = MathHelper.lerp(fadeCount/fadeTime, initVolume, 0.0).toFloat()
            if (volume <= 0) this.done = true
        }
    }
}
