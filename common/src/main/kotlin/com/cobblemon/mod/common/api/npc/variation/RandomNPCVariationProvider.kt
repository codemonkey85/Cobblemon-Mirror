/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.variation

import com.cobblemon.mod.common.entity.npc.NPCEntity

/**
 * A super basic random NPC variation provider but is mainly used to simply communicate variations to the client.
 *
 * @author Hiroku
 * @since August 14th, 2024
 */
class RandomNPCVariationProvider(
    override val aspects: Set<String> = emptySet()
) : NPCVariationProvider {
    override fun provideAspects(npcEntity: NPCEntity) = setOf(aspects.random())
}