/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import net.minecraft.world.entity.schedule.Activity

object CobblemonActivities {
    val activities = mutableListOf<Activity>()
    val BATTLING_ACTIVITY = register(Activity("pokemon_battling"))
    val NPC_BATTLING = register(Activity("npc_battling"))
    val NPC_ACTION_EFFECT = register(Activity("npc_action_effect"))
    val NPC_CHATTING = register(Activity("npc_chatting"))

    fun register(activity: Activity): Activity {
        activities.add(activity)
        return activity
    }
}