/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.isInt
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import com.google.gson.JsonElement
import net.minecraft.network.RegistryFriendlyByteBuf

open class Learnset : ClientDataSynchronizer<Learnset> {
    class Interpreter(
        val loadMove: (JsonElement, Learnset) -> Boolean
    ) {
        companion object {
            fun parseFromPrefixIntoList(prefix: String, list: (Learnset) -> MutableList<MoveTemplate>): Interpreter {
                return Interpreter { element, learnset ->
                    val str = element.takeIf { it.isJsonPrimitive }?.asString ?: return@Interpreter false
                    if (str.startsWith(prefix)) {
                        Moves.getByName(str.substringAfter(":"))
                            ?.let {
                                list(learnset).add(it)
                                return@Interpreter true
                            }
                    }
                    return@Interpreter false
                }
            }
        }
    }

    companion object {
        val tmInterpreter = Interpreter.parseFromPrefixIntoList("tm") { it.tmMoves }
        val eggInterpreter = Interpreter.parseFromPrefixIntoList("egg") { it.eggMoves }
        val tutorInterpreter = Interpreter.parseFromPrefixIntoList("tutor") { it.tutorMoves }
        val formChangeInterpreter = Interpreter.parseFromPrefixIntoList("form_change") { it.formChangeMoves }
        val levelUpInterpreter = Interpreter { element, learnset ->
            val str = element.takeIf { it.isJsonPrimitive }?.asString ?: return@Interpreter false
            val splits = str.split(":")
            if (splits.size != 2) {
                return@Interpreter false
            } else if (!splits[0].isInt()) {
                return@Interpreter false
            }

            val level = splits[0].toInt()
            val moveName = splits[1]
            val move = Moves.getByName(moveName) ?: return@Interpreter false

            val levelLearnset = learnset.levelUpMoves.getOrPut(level) { mutableListOf() }
            if (move !in levelLearnset) {
                levelLearnset.add(move)
            }

            return@Interpreter true

        }

        val interpreters = mutableListOf(
            tmInterpreter,
            eggInterpreter,
            tutorInterpreter,
            levelUpInterpreter,
            formChangeInterpreter
        )
    }

    val levelUpMoves = mutableMapOf<Int, MutableList<MoveTemplate>>()
    val eggMoves = mutableListOf<MoveTemplate>()
    val tutorMoves = mutableListOf<MoveTemplate>()
    val tmMoves = mutableListOf<MoveTemplate>()
    /**
     * Moves the species/form will have learnt when evolving into itself.
     * These are dynamically resolved each boot.
     */
    val evolutionMoves = mutableSetOf<MoveTemplate>()
    val formChangeMoves = mutableListOf<MoveTemplate>()

    fun getLevelUpMovesUpTo(level: Int) = levelUpMoves
        .entries
        .filter { it.key <= level }
        .sortedBy { it.key }
        .flatMap { it.value }
        .toSet()

    // We only sync level up moves atm
    override fun shouldSynchronize(other: Learnset) = other.levelUpMoves != this.levelUpMoves

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        this.levelUpMoves.clear()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            val level = buffer.readSizedInt(IntSize.U_SHORT)
            val moves = mutableListOf<MoveTemplate>()
            repeat(times = buffer.readSizedInt(IntSize.U_SHORT)) {
                Moves.getByNumericalId(buffer.readInt())?.let(moves::add)
            }
            levelUpMoves[level] = moves
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, levelUpMoves.size)
        for ((level, moves) in levelUpMoves) {
            buffer.writeSizedInt(IntSize.U_SHORT, level)
            buffer.writeSizedInt(IntSize.U_SHORT, moves.size)
            for (move in moves) {
                buffer.writeInt(move.num)
            }
        }
    }
}