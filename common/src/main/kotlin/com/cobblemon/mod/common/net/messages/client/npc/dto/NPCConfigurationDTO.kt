/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.npc.dto

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.*
import com.mojang.datafixers.util.Either
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class NPCConfigurationDTO : Encodable, Decodable {
    var npcName: MutableComponent = "".text()
    var npcClass: ResourceLocation = cobblemonResource("default")
    var battle: NPCBattleConfiguration? = null
    var interactionInherited: Boolean = false
    var interaction: NPCInteractConfiguration? = null
    var aspects: MutableSet<String> = mutableSetOf()

    constructor()

    constructor(npcEntity: NPCEntity) {
        npcName = npcEntity.name.copy()
        npcClass = npcEntity.npc.resourceIdentifier
        battle = npcEntity.battle
        interactionInherited = npcEntity.interaction == null
        interaction = npcEntity.interaction ?: npcEntity.npc.interaction
        aspects = npcEntity.appliedAspects
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeText(npcName)
        buffer.writeIdentifier(npcClass)
        buffer.writeNullable(battle) { _, value -> value.encode(buffer) }
        buffer.writeBoolean(interactionInherited)
        buffer.writeNullable(interaction) { _, value ->
            buffer.writeString(value.type)
            value.encode(buffer)
        }
        buffer.writeCollection(aspects, ByteBuf::writeString)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        npcName = buffer.readText().copy()
        npcClass = buffer.readIdentifier()
        battle = buffer.readNullable { NPCBattleConfiguration().apply { decode(buffer) } }
        interactionInherited = buffer.readBoolean()
        interaction = buffer.readNullable {
            val type = buffer.readString()
            val configType = NPCInteractConfiguration.types[type] ?: return@readNullable null
            configType.clazz.getConstructor().newInstance().also { it.decode(buffer) }
        }
        aspects = buffer.readList { buffer.readString() }.toMutableSet()
    }

    fun apply(entity: NPCEntity) {
        val npcClass =  NPCClasses.getByIdentifier(npcClass) ?: return Cobblemon.LOGGER.error("Failed to apply NPC class $npcClass")
        entity.customName = npcName.copy()
        entity.npc = npcClass
        entity.battle = battle
        if (!interactionInherited) {
            entity.interaction = interaction
        } else {
            entity.interaction = null
        }
        entity.appliedAspects.clear()
        entity.appliedAspects.addAll(aspects)
    }
}