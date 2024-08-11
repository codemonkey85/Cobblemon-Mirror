/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.mojang.serialization.Codec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ExtraCodecs

open class Learnset {
    class Interpreter(
        val loadMove: (String, Learnset) -> Boolean
    ) {
        companion object {
            fun parseFromPrefixIntoList(prefix: String, list: (Learnset) -> MutableList<MoveTemplate>): Interpreter {
                return Interpreter { string, learnset ->
                    if (string.startsWith(prefix)) {
                        Moves.get(string.substringAfter(":"))
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

        @JvmStatic
        val CODEC: Codec<Learnset> = Codec.STRING.listOf()
            .xmap({ list ->
                val learnset = Learnset()
                list.forEach { element ->
                    interpreters.forEach { interpreter ->
                        interpreter.loadMove(element, learnset)
                    }
                }
                learnset
            },
            { learnset ->
                val list = arrayListOf<String>()
                learnset.levelUpMoves.forEach { (level, moves) ->
                    moves.forEach { move ->
                        list.add("$level:${move.resourceLocation()}")
                    }
                }
                learnset.tmMoves.forEach { move ->
                    list.add("tm:${move.resourceLocation()}")
                }
                learnset.eggMoves.forEach { move ->
                    list.add("egg:${move.resourceLocation()}")
                }
                learnset.tutorMoves.forEach { move ->
                    list.add("tutor:${move.resourceLocation()}")
                }
                learnset.formChangeMoves.forEach { move ->
                    list.add("form_change:${move.resourceLocation()}")
                }
                list
            })

        @JvmStatic
        val CLIENT_CODEC: Codec<Learnset> = Codec.unboundedMap(ExtraCodecs.POSITIVE_INT, CobblemonRegistries.MOVE.byNameCodec().listOf())
            .xmap(
                { map ->
                    val learnset = Learnset()
                    learnset.levelUpMoves += map
                    learnset
                },
                Learnset::levelUpMoves
            )

        @JvmStatic
        val S2C_CODEC: StreamCodec<RegistryFriendlyByteBuf, Learnset> = ByteBufCodecs.fromCodecWithRegistriesTrusted(CLIENT_CODEC)

        val tmInterpreter = Interpreter.parseFromPrefixIntoList("tm") { it.tmMoves }
        val eggInterpreter = Interpreter.parseFromPrefixIntoList("egg") { it.eggMoves }
        val tutorInterpreter = Interpreter.parseFromPrefixIntoList("tutor") { it.tutorMoves }
        val formChangeInterpreter = Interpreter.parseFromPrefixIntoList("form_change") { it.formChangeMoves }
        val levelUpInterpreter = Interpreter { string, learnset ->
            val prefix = string.substringBefore(":")
            val level = prefix.toIntOrNull() ?: return@Interpreter false
            val moveId = string.substringAfter(":")
            val move = Moves.get(moveId) ?: return@Interpreter false
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

}