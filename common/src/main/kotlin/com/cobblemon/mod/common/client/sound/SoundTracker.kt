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
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Tracks [SoundInstance]s played on the client by an emitting object identified by [T].
 *
 * @author Segfault Guy
 * @since August 18th 2024
 */
abstract class SoundTracker<T> {

    private val manager = Minecraft.getInstance().soundManager

    /** Map of each [T]'s tracked [SoundInstance]s. Instances are not guaranteed to be active. */
    private val trackedSounds: HashMap<T, HashMap<ResourceLocation, SoundInstance>> = hashMapOf()

    /** Checks if there is an existing [SoundInstance] of the same [location] currently playing for [id]. */
    fun isActive(id: T, location: ResourceLocation) = trackedSounds.get(id)?.get(location)?.let { manager.isActive(it) } == true

    fun play(id: T, sound: SoundInstance) {
        val tracks = this.trackedSounds.computeIfAbsent(id) { hashMapOf() }
        tracks.get(sound.location)?.let { existing -> manager.stop(existing) }  // replace anything already playing
        tracks.put(sound.location, sound)
        manager.play(sound)
    }

    fun stop(id: T, sound: SoundInstance) = this.stop(id, sound.location)

    fun stop(id: T, location: ResourceLocation) {
        val emitter = trackedSounds.get(id) ?: return
        val existing = emitter.get(location) ?: return
        manager.stop(existing)
        emitter.remove(location)
    }

    fun stopAll() = trackedSounds.forEach { (_, tracks) ->
        tracks.forEach { (_, sound) -> manager.stop(sound) }
        tracks.clear()
    }

    fun stopAll(id: T) {
        val tracks = this.trackedSounds.get(id) ?: return
        tracks.forEach { (_, sound) -> this.manager.stop(sound) }
        tracks.clear()
    }

    fun clear() {
        this.stopAll()
        this.trackedSounds.clear()
    }

    /** Removes [id] and its tracked [SoundInstance]s from the tracker. */
    fun clear(id: T) {
        this.stopAll(id)
        this.trackedSounds.remove(id)
    }
    // this should be called whenever the emitter is unloaded from the client

    companion object {

        /** Stops all active [SoundInstance]s that are tracked by a [SoundTracker]. */
        fun stopAll() {
            BlockEntitySoundTracker.stopAll()
            EntitySoundTracker.stopAll()
        }

        /** Clears all [SoundTracker]s. */
        fun clear() {
            this.stopAll()
            BlockEntitySoundTracker.clear()
            EntitySoundTracker.clear()
        }
    }
}

/** [SoundTracker] for [BlockEntity]s, identified by their unique block position. */
object BlockEntitySoundTracker : SoundTracker<BlockPos>()

/** [SoundTracker] for [Entity]s, identified by their unique [Entity.id]. */
object EntitySoundTracker : SoundTracker<Int>()
