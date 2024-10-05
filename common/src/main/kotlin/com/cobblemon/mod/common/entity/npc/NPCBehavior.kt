/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc

import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.npc.configuration.NPCConfigVariable
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.entity.npc.NPCBrain.BrainBuilder
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.Brain

sealed interface NPCBehavior {
    fun getConfigVariables(): List<NPCConfigVariable>
    fun configure(npcEntity: NPCEntity, brain: Brain<out NPCEntity>, builder: BrainBuilder)
    fun encode(buffer: RegistryFriendlyByteBuf)
    fun decode(buffer: RegistryFriendlyByteBuf)


    companion object {
        val types = mutableMapOf<String, Class<out NPCBehavior>>(
            "script" to CustomNPCBehavior::class.java,
        )
    }
}


class ScriptedNPCBehavior : NPCBehavior {
    val script: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "dummy")
    val variables = mutableListOf<NPCConfigVariable>()

    override fun getConfigVariables() = variables
    override fun configure(npcEntity: NPCEntity, brain: Brain<out NPCEntity>, builder: BrainBuilder) {
        CobblemonScripts.run(script, npcEntity.runtime.withQueryValue("brain", NPCBrain.createNPCBrainStruct(npcEntity, brain, builder)))
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        TODO("Not yet implemented")
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        TODO("Not yet implemented")
    }
}

interface CustomNPCBehavior : NPCBehavior {


}
