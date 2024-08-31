/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Rules around how a battle is going to work.
 *
 * @author Hiroku
 * @since March 9th, 2022
 */
data class BattleFormat(
    val mod: String = "cobblemon",
    val battleType: BattleType = BattleTypes.SINGLES,
    val ruleSet: Set<String> = setOf(),
    val gen: Int = 9
) {
    companion object {
        val GEN_9_SINGLES = BattleFormat(
            battleType = BattleTypes.SINGLES,
            ruleSet = setOf(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)
        )

        val GEN_9_DOUBLES = BattleFormat(
            battleType = BattleTypes.DOUBLES,
            ruleSet = setOf(BattleRules.OBTAINABLE)
        )

        val GEN_9_MULTI = BattleFormat(
            battleType = BattleTypes.MULTI,
            ruleSet = setOf(BattleRules.OBTAINABLE)
        )

        fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): BattleFormat {
            val mod = buffer.readString()
            val battleType = BattleType.loadFromBuffer(buffer)
            val ruleSet = mutableSetOf<String>()
            repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) { ruleSet.add(buffer.readString()) }
            return BattleFormat(
                mod = mod,
                battleType = battleType,
                ruleSet = ruleSet
            )
        }
    }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf): RegistryFriendlyByteBuf {
        buffer.writeString(mod)
        battleType.saveToBuffer(buffer)
        buffer.writeSizedInt(IntSize.U_BYTE, ruleSet.size)
        ruleSet.forEach(buffer::writeString)
        return buffer
    }

    fun toFormatJSON(): String {
        return """
            {
                "mod": "$mod",
                "gameType": "${battleType.name}",
                "gen": $gen,
                "ruleset": [${ruleSet.joinToString { "\"$it\"" }}],
                "effectType": "Format"
            }
        """.trimIndent().replace("\n", "")
    }
}