/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves.entry.variant

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.moves.entry.LearnsetEntry
import com.cobblemon.mod.common.api.pokemon.moves.entry.LearnsetEntryType
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

class LevelUpLearnsetEntry(override val move: Holder<MoveTemplate>, val level: Int) : LearnsetEntry {

    override val type: LearnsetEntryType<*> = LearnsetEntryType.LEVEL_UP

    override val syncToClient: Boolean = true

    companion object {
        @JvmStatic
        val CODEC: MapCodec<LevelUpLearnsetEntry> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MoveTemplate.CODEC.fieldOf("move").forGetter(LevelUpLearnsetEntry::move),
                CodecUtils.dynamicIntRange(0) { Cobblemon.config.maxPokemonLevel }.fieldOf("level").forGetter(LevelUpLearnsetEntry::level),
            ).apply(instance, ::LevelUpLearnsetEntry)
        }
    }

}