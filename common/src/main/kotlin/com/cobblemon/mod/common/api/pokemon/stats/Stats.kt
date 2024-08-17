/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.stats

import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import java.util.EnumSet
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * An enumeration of the default implemented [Stat]s.
 * Contains all the traditional stats in official Pokémon games.
 */
enum class Stats(override val identifier: ResourceLocation, override val displayName: Component, override val type: Stat.Type, override val showdownId: String) : Stat {

    HP(cobblemonResource("hp"), lang("stat.hp.name"), Stat.Type.PERMANENT, "hp"),
    ATTACK(cobblemonResource("attack"), lang("stat.attack.name"), Stat.Type.PERMANENT, "atk"),
    DEFENCE(cobblemonResource("defence"), lang("stat.defence.name"), Stat.Type.PERMANENT, "def"),
    SPECIAL_ATTACK(cobblemonResource("special_attack"), lang("stat.special_attack.name"), Stat.Type.PERMANENT, "spa"),
    SPECIAL_DEFENCE(cobblemonResource("special_defence"), lang("stat.special_defence.name"), Stat.Type.PERMANENT, "spd"),
    SPEED(cobblemonResource("speed"), lang("stat.speed.name"), Stat.Type.PERMANENT, "spe"),
    EVASION(cobblemonResource("evasion"), lang("stat.evasion.name"), Stat.Type.BATTLE_ONLY, "evasion"),
    ACCURACY(cobblemonResource("accuracy"), lang("stat.accuracy.name"), Stat.Type.BATTLE_ONLY, "accuracy");

    companion object {

        /**
         * All the stats, an alternative to [values].
         * Using [StatProvider.all] is recommended instead for maximum addon compatibility.
         */
        val ALL: Set<Stat> = EnumSet.allOf(Stats::class.java)

        /**
         * All the stats with type of [Stat.Type.PERMANENT].
         * Using [StatProvider.ofType] with type [Stat.Type.PERMANENT] is recommended instead for maximum addon compatibility.
         */
        val PERMANENT: Set<Stat> = EnumSet.of(HP, ATTACK, DEFENCE, SPECIAL_ATTACK, SPECIAL_DEFENCE, SPEED)

        /**
         * All the stats with type of [Stat.Type.BATTLE_ONLY].
         * Using [StatProvider.ofType] with type [Stat.Type.BATTLE_ONLY] is recommended instead for maximum addon compatibility.
         */
        val BATTLE_ONLY: Set<Stat> = EnumSet.of(EVASION, ACCURACY)

        /** Gets the [Stat] from the respective Showdown id. */
        fun getStat(statKey: String) = when(statKey) {
            "hp" -> HP
            "atk", "Attack" -> ATTACK // Hyper Cutter states the full stat name "Attack"
            "def", "Defense" -> DEFENCE // Big Pecks states the full stat name "Defense"
            "spa" -> SPECIAL_ATTACK
            "spd" -> SPECIAL_DEFENCE
            "spe" -> SPEED
            "evasion" -> EVASION
            else -> ACCURACY
        }

        /** Gets the severity lang key from the respective boost/unboost stage. */
        fun getSeverity(stages: Int) = when(stages) {
            0 -> "cap.single"
            1 -> "slight"
            2 -> "sharp"
            else -> "severe"
        }

    }

}