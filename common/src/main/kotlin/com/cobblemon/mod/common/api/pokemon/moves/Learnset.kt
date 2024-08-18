/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.moves.entry.LearnsetEntry
import com.cobblemon.mod.common.api.pokemon.moves.entry.variant.*
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec

class Learnset(
    val holders: Set<LearnsetEntry>
) {

    val levelUpMoves: Map<Int, Set<MoveTemplate>> by lazy {
        val entries = this.holders.filterIsInstance<LevelUpLearnsetEntry>()
        val map = hashMapOf<Int, MutableSet<MoveTemplate>>()
        entries.forEach { entry ->
            map.getOrPut(entry.level) { hashSetOf() }.add(entry.move.value())
        }
        map
    }

    val eggMoves by lazy {
        this.holders.filterIsInstance<EggLearnsetEntry>()
            .map { it.move.value() }
            .toSet()
    }

    val tutorMoves by lazy {
        this.holders.filterIsInstance<TutorLearnsetEntry>()
            .map { it.move.value() }
            .toSet()
    }

    val tmMoves by lazy {
        this.holders.filterIsInstance<TmLearnsetEntry>()
            .map { it.move.value() }
            .toSet()
    }
    /**
     * Moves the species/form will have learnt when evolving into itself.
     * These are dynamically resolved each boot.
     */
    val evolutionMoves = mutableSetOf<MoveTemplate>()

    val formChangeMoves by lazy {
        this.holders.filterIsInstance<FormChangeLearnsetEntry>()
            .map { it.move.value() }
            .toSet()
    }

    fun getLevelUpMovesUpTo(level: Int) = levelUpMoves
        .entries
        .filter { it.key <= level }
        .sortedBy { it.key }
        .flatMap { it.value }
        .toSet()

    companion object {

        @JvmStatic
        val CODEC: Codec<Learnset> = CodecUtils.setOf(LearnsetEntry.CODEC)
            .xmap(::Learnset, Learnset::holders)

        @JvmStatic
        val CLIENT_CODEC: Codec<Learnset> = CodecUtils.setOf(LearnsetEntry.CODEC)
            .xmap(
                { set -> Learnset(set) },
                { learnset -> learnset.holders.filterIsInstance<LevelUpLearnsetEntry>().toSet() }
            )

    }

}