/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves.entry

import com.cobblemon.mod.common.api.pokemon.moves.entry.variant.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

fun interface LearnsetEntryType<T : LearnsetEntry> {

    fun codec(): MapCodec<T>

    companion object {

        internal val REGISTRY: Registry<LearnsetEntryType<*>> = MappedRegistry(
            ResourceKey.createRegistryKey(cobblemonResource("learnset_entry")),
            Lifecycle.stable()
        )

        @JvmStatic
        val LEVEL_UP = this.register(cobblemonResource("level"), LevelUpLearnsetEntry.CODEC)

        @JvmStatic
        val EGG = this.register(cobblemonResource("egg"), EggLearnsetEntry.CODEC)

        @JvmStatic
        val TM = this.register(cobblemonResource("tm"), TmLearnsetEntry.CODEC)

        @JvmStatic
        val TUTOR = this.register(cobblemonResource("tutor"), TutorLearnsetEntry.CODEC)

        @JvmStatic
        val FORM_CHANGE = this.register(cobblemonResource("form_change"), FormChangeLearnsetEntry.CODEC)

        @JvmStatic
        fun <T : LearnsetEntry> register(id: ResourceLocation, codec: MapCodec<T>): LearnsetEntryType<T> {
            return Registry.register(REGISTRY, id, LearnsetEntryType { codec })
        }

    }
}