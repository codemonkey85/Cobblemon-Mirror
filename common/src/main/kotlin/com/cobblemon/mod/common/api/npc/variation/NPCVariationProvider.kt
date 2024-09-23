/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.variation

import com.cobblemon.mod.common.entity.npc.NPCEntity

// TODO probably need an exclusion list of aspects to prevent double application
// also some way to add them from a command?

/**
 * An NPC variation provider happens at the moment of generating a particular NPC and works by appending to the
 * aspects list that gets locked onto the NPC. Then ideally they can be tweaked by an editor or code after they've
 * been put on the NPC. Retaining knowledge of the variation provider might be necessary for presenting a clear
 * way of moving between different options but might be out of scope.
 *
 * @author Hiroku
 * @since August 10th, 2024
 */
interface NPCVariationProvider {
    /** All of the possible aspects that could be provided. */
    val aspects: Set<String>
    /** Provides some number of aspects for the given entity. */
    fun provideAspects(npcEntity: NPCEntity): Set<String>

    companion object {
        val types = mutableMapOf(
            "weighted" to WeightedNPCVariationProvider::class.java,
            "random" to RandomNPCVariationProvider::class.java
        )

        fun register(type: String, clazz: Class<out NPCVariationProvider>) {
            types[type] = clazz
        }
    }
}