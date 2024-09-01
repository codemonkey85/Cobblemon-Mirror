/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.duck.SoundManagerDuck
import net.minecraft.client.sounds.SoundManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource

/**
 * Pauses the audio [Source]s belonging to the queried [SoundInstance]s.
 *
 * @param id The [ResourceLocation] of the [SoundInstance] to pause. If null, will pause all sounds belonging to the specified [category].
 * @param category The [SoundSource] that [id] is queried from. If null, will query [id] from all categories.
 */
fun SoundManager.pauseSounds(id: ResourceLocation?, category: SoundSource?) = (this as SoundManagerDuck).pauseSounds(id, category)

/**
 * Resumes the audio [Source]s belonging to the queried [SoundInstance]s.
 *
 * @param id The [ResourceLocation] of the [SoundInstance] to resume. If null, will resume all sounds belonging to the specified [category].
 * @param category The [SoundSource] that [id] is queried from. If null, will query [id] from all categories.
 */
fun SoundManager.resumeSounds(id: ResourceLocation?, category: SoundSource?) = (this as SoundManagerDuck).resumeSounds(id, category)
