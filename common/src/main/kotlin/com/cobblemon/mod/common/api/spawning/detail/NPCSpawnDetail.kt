/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.api.npc.NPCConfiguration
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.google.gson.annotations.SerializedName

/**
 * A [SpawnDetail] describing an [NPCEntity] spawn.
 *
 * @author Hiroku
 * @since October 8th, 2023
 */
class NPCSpawnDetail : SpawnDetail() {
    companion object {
        val TYPE = "npc"
        val blankClass = NPCClass()
    }

    override val type = TYPE

    @SerializedName(value = "npcClass", alternate = ["class", "npc"])
    val npcClass: NPCClass = blankClass
    val configuration: NPCConfiguration? = null
    val aspects: Set<String> = emptySet()

    override fun doSpawn(ctx: SpawningContext) = NPCSpawnAction(ctx, this)
}