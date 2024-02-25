/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.pokedex.trackeddata.FormTrackedData
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Identifier

/**
 * TrackedData of a specific form of a specific species
 *
 * @author Apion
 * @since February 24, 2024
 */
class FormPokedexEntry(var knowledge: PokedexProgress = PokedexProgress.NONE) {
    var formStats = mutableSetOf<FormTrackedData>()

    companion object {
        val CODEC: Codec<FormPokedexEntry> = RecordCodecBuilder.create { instance ->
            instance.group(
                PokedexProgress.CODEC.fieldOf("knowledge").forGetter { it.knowledge }
            ).apply(instance, ::FormPokedexEntry)
        }
    }
}