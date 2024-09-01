/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties

const val QUOTE = '"'

fun String.splitMap(delimiter: String, assigner: String) : MutableList<Pair<String, String?>> {
    val result = mutableListOf<Pair<String, String?>>()
    val split = this.split(delimiter)

    var joiner: String? = null
    for (argument in split) {
        if (joiner != null && argument.endsWith(QUOTE)) {
            joiner += "$delimiter${argument.substring(0, argument.length - 1)}"

            val components = joiner.split(assigner)
            val key = components[0].lowercase()
            val value = if (joiner.contains(assigner)) {
                components[1]
            } else {
                null
            }

            joiner = null
            result.add(key to value)
        } else if (joiner == null) {
            if (argument.contains(assigner)) {
                val equalsIndex = argument.indexOf(assigner)
                val key = argument.substring(0, equalsIndex).lowercase()
                val value = argument.substring(equalsIndex + 1)

                if (value.startsWith(QUOTE)) {
                    if (value.endsWith(QUOTE)) {
                        result.add(key to value.substring(1, value.length - 1))
                    } else {
                        joiner = "$key$assigner${value.substring(1)}"
                    }
                } else {
                    result.add(key to value)
                }
            } else {
                if (argument.startsWith(QUOTE) && argument.endsWith(QUOTE)) {
                    result.add(argument.lowercase().substring(1, argument.length - 1) to null)
                } else if(!argument.contains(QUOTE)) {
                    result.add(argument.lowercase() to null)
                }
            }
        } else {
            joiner += "$delimiter$argument"
        }
    }

    return result
}

fun String.isLaterVersion(otherVersion: String): Boolean {
    if (this === otherVersion) {
        return false
    }

    val splits1 = this.split(".")
    val splits2 = otherVersion.split(".")

    var smaller = if (splits1.size > splits2.size) this else otherVersion

    for (i in 0 until smaller.split(".").size) {
        try {
            val v1 = splits1[i].toInt()
            val v2 = splits2[i].toInt()

            if (v1 > v2) {
                return true;
            } else if (v2 > v1) {
                return false;
            }
        } catch (e: NumberFormatException) {
            Cobblemon.LOGGER.error("Tried comparing versions $this and $otherVersion but at least one of them isn't formatted like a version.")
            return false
        }
    }

    return smaller != this
}

fun String.toProperties() = PokemonProperties.parse(this)
fun String.toPokemon() = toProperties().create()
fun String.endWith(suffix: String) = if (endsWith(suffix)) this else "$this$suffix"
fun String.startWith(prefix: String) = if (startsWith(prefix)) this else "$prefix$this"