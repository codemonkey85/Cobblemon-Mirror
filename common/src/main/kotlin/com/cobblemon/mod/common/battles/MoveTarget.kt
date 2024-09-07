/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import kotlin.math.abs


interface Targetable {
    fun getAllActivePokemon(): Iterable<Targetable>
    fun getActorPokemon(): Iterable<Targetable>
    fun getSidePokemon(): Iterable<Targetable>
    fun getFormat(): BattleFormat
    fun isAllied(other: Targetable): Boolean
    fun hasPokemon(): Boolean
    fun getActorShowdownId(): String
    fun getPNX() = "${getActorShowdownId()}${getLetter()}"

    fun getAdjacent(): List<Targetable> {
        val digit = getDigit()
        val sideSize = getFormat().battleType.pokemonPerSide
        return getAllActivePokemon().filter {
            val sameSideDigit = if (it.isAllied(this)) {
                it.getDigit()
            } else {
                sideSize - it.getDigit() + 1
            }
            val digitDistance = abs(sameSideDigit - digit)
            return@filter digitDistance <= 1 && it != this
        }
    }

    fun getAdjacentAllies() = getAdjacent().filter { it.isAllied(this) }
    fun getAdjacentOpponents() = getAdjacent().filterNot { it.isAllied(this) }

    fun getSignedDigitRelativeTo(other: Targetable): String {
        val digit = getDigitRelativeTo(other)
        return if (isAllied(other)) {
            "-$digit"
        } else {
            "+$digit"
        }
    }
    fun getDigitRelativeTo(other: Targetable) = getDigit(asAlly = isAllied(other))
    fun getDigit(asAlly: Boolean = true): Int {
        var digit = 1
        for (activePokemon in getSidePokemon()) {
            if (activePokemon == this) {
                return digit
            } else {
                digit++
            }
        }
        return digit * if (asAlly) 1 else -1
    }

    fun getLetter(): Char {
        var index = 0
        for (activePokemon in getSidePokemon()) {
            if (activePokemon == this) {
                break
            } else {
                index++
            }
        }

        return when (index) {
            0 -> 'a'
            1 -> 'b'
            2 -> 'c'
            3 -> 'd'
            4 -> 'e'
            5 -> 'f'
            else -> throw IllegalStateException("Battle has more than 6 in the active slot, makes no sense.")
        }
    }

    /*
     * Gets the list of valid targets for a move that the user is not allowed to select a target
     * This is needed for the target menu to show the client what targets will be hit in a
     * multitarget move.
     */
    fun getMultiTargetList(targetType: MoveTarget) : List<Targetable>? {
        return when (targetType) {
            MoveTarget.any -> null
            MoveTarget.all -> getAllActivePokemon().toList()
            MoveTarget.allAdjacent -> getAdjacent()
            MoveTarget.allAdjacentFoes -> getAdjacentOpponents()
            MoveTarget.self -> listOf(this)
            MoveTarget.normal -> null
            MoveTarget.randomNormal -> listOf(this)
            MoveTarget.allies -> getAllActivePokemon().filter { it.isAllied(this) }
            MoveTarget.allySide -> getAllActivePokemon().filter { it.isAllied(this) }
            MoveTarget.allyTeam -> getAllActivePokemon().filter { it.isAllied(this) }
            MoveTarget.adjacentAlly -> null
            MoveTarget.adjacentAllyOrSelf -> null
            MoveTarget.adjacentFoe -> null
            MoveTarget.foeSide -> getAllActivePokemon().filter { !it.isAllied(this) }
            MoveTarget.scripted -> null
            else -> null
        }
    }
}

enum class MoveTarget(val targetList: (Targetable) -> List<Targetable>? = { null }) {
    any({ pokemon -> pokemon.getAllActivePokemon().filter { it != pokemon } }),
    all,
    allAdjacent,
    allAdjacentFoes,
    self,
    normal({ pokemon -> pokemon.getAdjacent() }),
    randomNormal,
    allies,
    allySide,
    allyTeam,
    adjacentAlly({ pokemon -> pokemon.getAdjacentAllies() }),
    adjacentAllyOrSelf({ pokemon -> pokemon.getAdjacentAllies() + pokemon }),
    adjacentFoe({ pokemon -> pokemon.getAdjacentOpponents() }),
    foeSide,
    scripted;


    companion object {

        private val VALUES = values()

        /**
         * Attempts to parse a [MoveTarget] from the given [showdownId].
         *
         * @param showdownId The showdown ID fo the move target.
         *
         * @throws NoSuchElementException if the value cannot be found.
         */
        fun fromShowdownId(showdownId: String): MoveTarget = VALUES.first { target -> target.name.equals(showdownId, true) }

    }

}