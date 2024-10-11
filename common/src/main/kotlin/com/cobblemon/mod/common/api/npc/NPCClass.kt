/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCConfigVariable
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.npc.variation.NPCVariationProvider
import com.cobblemon.mod.common.api.npc.variation.RandomNPCVariationProvider
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityDimensions

/**
 * A class of NPC. This can contain a lot of preset information about the NPC's behaviour. Consider this the Pokémon
 * species but for NPCs.
 *
 * @author Hiroku
 * @since August 14th, 2023
 */
class NPCClass {
    @Transient
    lateinit var id: ResourceLocation

    var resourceIdentifier: ResourceLocation = cobblemonResource("dummy")
    var names: MutableList<Component> = mutableListOf()
    var aspects: MutableSet<String> = mutableSetOf() // These only make sense when applied via presets
    var hitbox = EntityDimensions.scalable(0.6F, 1.8F)
    var battleConfiguration = NPCBattleConfiguration()
    var aiScripts: MutableList<ResourceLocation> = mutableListOf()
    var interaction: NPCInteractConfiguration? = null
    var variations: MutableMap<String, NPCVariationProvider> = mutableMapOf()
    var config: MutableList<NPCConfigVariable> = mutableListOf()
    var variables = mutableMapOf<String, MoValue>() // Questionable whether this should be here.
    var party: NPCPartyProvider? = null
    var skill: Int = 0

    // If you're adding stuff here, add it to NPCPreset and NPCClassAdapter too

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(resourceIdentifier.toString())
        buffer.writeCollection(names) { _, v -> buffer.writeText(v) }
        buffer.writeFloat(this.hitbox.width)
        buffer.writeFloat(this.hitbox.height)
        buffer.writeBoolean(this.hitbox.fixed)
        battleConfiguration.encode(buffer)
        buffer.writeCollection(aiScripts) { _, v -> buffer.writeString(v.toString()) }
        buffer.writeNullable(interaction) { _, value ->
            buffer.writeString(value.type)
            value.encode(buffer)
        }
        buffer.writeSizedInt(IntSize.U_BYTE, variations.size)
        for ((key, value) in variations) {
            buffer.writeString(key)
            val aspects = value.aspects
            buffer.writeCollection(aspects) { _, v -> buffer.writeString(v) }
        }
        buffer.writeCollection(config) { _, v ->
            buffer.writeString(v.variableName)
            buffer.writeText(v.displayName)
            buffer.writeText(v.description)
            buffer.writeEnumConstant(v.type)
            buffer.writeString(v.defaultValue)
        }
        buffer.writeInt(skill)
        buffer.writeMapK(size = IntSize.U_BYTE, map = variables) { (key, value) ->
            buffer.writeString(key)
            buffer.writeString(value.asString())
        }
    }

    fun decode(buffer: RegistryFriendlyByteBuf) {
        resourceIdentifier = ResourceLocation.parse(buffer.readString().toString())
        names = buffer.readList { buffer.readText().copy() }.toMutableList()
        val length = buffer.readFloat()
        val width = buffer.readFloat()
        val fixed = buffer.readBoolean()
        hitbox = if (fixed) EntityDimensions.fixed(length, width) else EntityDimensions.scalable(length, width)
        battleConfiguration = NPCBattleConfiguration()
        battleConfiguration.decode(buffer)
        aiScripts = buffer.readList { ResourceLocation.parse(buffer.readString()) }.toMutableList()
        interaction = buffer.readNullable {
            val type = buffer.readString()
            val configType = NPCInteractConfiguration.types[type] ?: return@readNullable null
            val instance = configType.clazz.getConstructor().newInstance()
            instance.decode(buffer)
            instance
        }
        val variationSize = buffer.readSizedInt(IntSize.U_BYTE)
        for (i in 0 until variationSize) {
            val key = buffer.readString()
            val aspects = buffer.readList { buffer.readString() }.toSet()
            val provider = RandomNPCVariationProvider(aspects)
            variations[key] = provider
        }
        config = buffer.readList {
            val variableName = buffer.readString()
            val displayName = buffer.readText()
            val description = buffer.readText()
            val type = buffer.readEnumConstant(NPCConfigVariable.NPCVariableType::class.java)
            val defaultValue = buffer.readString()
            NPCConfigVariable(variableName, displayName, description, type, defaultValue)
        }.toMutableList()
        skill = buffer.readInt()
        buffer.readMapK(size = IntSize.U_BYTE, map = variables) {
            val key = buffer.readString()
            val value = buffer.readString()
            if (value.toDoubleOrNull() != null) {
                return@readMapK key to DoubleValue(value.toDouble())
            } else {
                return@readMapK key to StringValue(value)
            }
        }
    }
}