/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.npc.NPCPreset
import com.cobblemon.mod.common.api.npc.NPCPresets
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.npc.variation.NPCVariationProvider
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.asResource
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.normalizeToArray
import com.cobblemon.mod.common.util.singularToPluralList
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import net.minecraft.world.entity.EntityDimensions

/**
 * Manually deserializes an NPCClass from JSON. Searches for [NPCPreset]s to apply as a base before applying the
 * properties of the class's JSON.
 *
 * @author Hiroku
 * @since August 11th, 2024
 */
object NPCClassAdapter : JsonDeserializer<NPCClass> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): NPCClass {
        val obj = json.asJsonObject
        val presets = obj.getAsJsonArray("presets")?.mapNotNull {
            val preset = NPCPresets.getPreset(it.asString.asResource())
            if (preset == null) {
                Cobblemon.LOGGER.error("NPC preset ${it.asString} not found")
            }
            preset
        } ?: emptySet()

        val npcClass = NPCClass()
        for (preset in presets) {
            preset.applyTo(npcClass)
        }

        obj.singularToPluralList("name")
        obj.get("names")?.let { npcClass.names = it.normalizeToArray().map { it.asString.asTranslated() }.toMutableList() }
        obj.get("resourceIdentifier")?.let { npcClass.resourceIdentifier = it.asString.asIdentifierDefaultingNamespace() }
        obj.get("aspects")?.let { npcClass.aspects = it.normalizeToArray().map { it.asString }.toMutableSet() }
        obj.get("variation")?.let { it.asJsonObject.entrySet().forEach { (key, value) -> npcClass.variations[key] = ctx.deserialize(value, NPCVariationProvider::class.java) } }
        obj.get("hitbox")?.let { npcClass.hitbox = ctx.deserialize(it, EntityDimensions::class.java) }
        obj.get("battleConfiguration")?.let { npcClass.battleConfiguration = ctx.deserialize(it, NPCBattleConfiguration::class.java) }
        obj.get("interaction")?.let { npcClass.interaction = ctx.deserialize(it, NPCInteractConfiguration::class.java) }
        obj.get("canDespawn")?.let { npcClass.canDespawn = it.asBoolean }
        obj.get("variations")?.let {
            val obj = it.asJsonObject
            obj.entrySet().forEach { (key, value) ->
                val provider = ctx.deserialize<NPCVariationProvider>(value, NPCVariationProvider::class.java)
                npcClass.variations[key] = provider
            }
        }
        obj.get("party")?.let { npcClass.party = ctx.deserialize(it, NPCPartyProvider::class.java) }
        obj.get("skill")?.let { npcClass.skill = it.asInt }
        obj.get("battleTheme")?.let { npcClass.battleTheme = it.asString.asIdentifierDefaultingNamespace() }

        return npcClass
    }
}