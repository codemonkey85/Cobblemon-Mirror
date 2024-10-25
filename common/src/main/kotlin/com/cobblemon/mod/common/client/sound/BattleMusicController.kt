/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.sound

import com.cobblemon.mod.common.client.sound.instances.BattleMusicInstance
import com.cobblemon.mod.common.util.pauseSounds
import com.cobblemon.mod.common.util.resumeSounds
import net.minecraft.client.Minecraft
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents

/**
 * The controller for playing, switching, and stopping [BattleMusicInstance]s.
 *
 * @author Segfault Guy
 * @since October 31st, 2023
 */
object BattleMusicController {

    /** The last [BattleMusicInstance] played. */
    var music = BattleMusicInstance(SoundEvents.EMPTY, 0.0F, 0.0F)
        private set

    /**
     * The [SoundSource]s that are filtered while the [SoundManager] is playing a [BattleMusicInstance].
     *
     * Applicable [SoundInstance]s are blocked while a [BattleMusicInstance] is active, or paused if played before.
     */
    val filteredCategories = listOf(SoundSource.AMBIENT, SoundSource.MUSIC, SoundSource.RECORDS)

    private val manager = Minecraft.getInstance().soundManager

    /** Start a new [BattleMusicInstance] and pause all [filteredCategories] sounds currently playing. */
    fun initializeMusic(newMusic: BattleMusicInstance) {
        music = newMusic
        manager.play(music)
        if (manager.isActive(music)) {
            filteredCategories.forEach { manager.pauseSounds(null, it) }
            manager.resumeSounds(music.location, SoundSource.MUSIC) // lazy :)
        }
    }

    /** Switch to a new [BattleMusicInstance] if one is already playing. */
    fun switchMusic(newMusic: BattleMusicInstance) {
        manager.stop(music)
        music = newMusic
        manager.play(music)
    }

    /** Fade out and end the current [BattleMusicInstance]. */
    fun endMusic() = music.setFade()

}
