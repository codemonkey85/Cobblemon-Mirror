/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc

import com.cobblemon.mod.common.api.ai.config.BrainConfig
import com.cobblemon.mod.common.api.npc.configuration.NPCConfigVariable
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.npc.variation.NPCVariationProvider
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityDimensions

class NPCPreset {
    lateinit var id: ResourceLocation
    var resourceIdentifier: ResourceLocation? = null
    var aspects: Set<String>? = null
    var variations: Map<String, NPCVariationProvider>? = null
    var config: List<NPCConfigVariable>? = null
    var party: NPCPartyProvider? = null
    var canDespawn: Boolean? = null
    var interaction: NPCInteractConfiguration? = null
    var names: MutableSet<Component>? = null
    var hitbox: EntityDimensions? = null
    var ai: List<BrainConfig>? = null
    var skill: Int? = null
    var battleTheme: ResourceLocation? = null

    fun applyTo(npcClass: NPCClass) {
        resourceIdentifier?.let { npcClass.resourceIdentifier = it }
        aspects?.let { npcClass.aspects.addAll(it) }
        config?.forEach {
            val variableName = it.variableName
            npcClass.config.removeIf { it.variableName == variableName }
            npcClass.config.add(it)
        }
        variations?.entries?.forEach { (key, value) -> npcClass.variations[key] = value }
        party?.let { npcClass.party = it }
        canDespawn?.let { npcClass.canDespawn = it }
//        aiScripts?.let { npcClass.aiScripts.addAll(it) }
        interaction?.let { npcClass.interaction = it }
        names?.let { npcClass.names.addAll(it) }
        hitbox?.let { npcClass.hitbox = it }
        skill?.let { npcClass.skill = it }
        battleTheme?.let { npcClass.battleTheme = it }
        ai?.let { npcClass.ai.addAll(it) }
    }
}