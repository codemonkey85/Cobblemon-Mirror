/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.world.entity.ai.goal.Goal
import java.util.EnumSet

/**
 * AI goal that stops movement tasks while the NPC is in battle.
 *
 * @author Hiroku
 * @since August 25th, 2023
 */
class StayPutInBattleGoal(val npc: NPCEntity) : Goal() {
    override fun canUse() = npc.isInBattle()

    override fun getFlags() = EnumSet.of(Flag.MOVE)
}