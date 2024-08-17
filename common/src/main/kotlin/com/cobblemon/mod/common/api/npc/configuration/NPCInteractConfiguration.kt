/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration

import com.cobblemon.mod.common.api.npc.configuration.interaction.CustomScriptNPCInteractionConfiguration
import com.cobblemon.mod.common.api.npc.configuration.interaction.DialogueNPCInteractionConfiguration
import com.cobblemon.mod.common.api.npc.configuration.interaction.NoneNPCInteractionConfiguration
import com.cobblemon.mod.common.api.npc.configuration.interaction.ScriptNPCInteractionConfiguration
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

/**
 * A type of interaction handler for when a player right-clicks the NPC.
 *
 * @author Hiroku
 * @since August 19th, 2023
 */
interface NPCInteractConfiguration {
    class NPCInteractConfigurationType<T : NPCInteractConfiguration>(
        val displayName: MutableComponent,
        val clazz: Class<T>
    )

    companion object {
        val types = mutableMapOf<String, NPCInteractConfigurationType<out NPCInteractConfiguration>>()

        fun register(type: String, displayName: MutableComponent, clazz: Class<out NPCInteractConfiguration>) {
            types[type] = NPCInteractConfigurationType(displayName, clazz)
        }

        init {
            register(type = "script", displayName = "Script".text(), clazz = ScriptNPCInteractionConfiguration::class.java)
            register(type = "custom_script", displayName = "Custom Script".text(), clazz = CustomScriptNPCInteractionConfiguration::class.java)
            register(type = "dialogue", displayName = "Dialogue".text(), clazz = DialogueNPCInteractionConfiguration::class.java)
            register(type = "none", displayName = "None".text(), clazz = NoneNPCInteractionConfiguration::class.java)
        }
    }

    val type: String
    fun interact(npc: NPCEntity, player: ServerPlayer): Boolean
    /** Don't add anything to this if you aren't registering the thing on the client as well. */
    fun encode(buffer: RegistryFriendlyByteBuf)
    /** Don't add anything to this if you aren't registering the thing on the client as well. */
    fun decode(buffer: RegistryFriendlyByteBuf)
    fun writeToNBT(compoundTag: CompoundTag)
    fun readFromNBT(compoundTag: CompoundTag)
    /** Returns true if the given configuration is considered different to this one. If false, the new one will not replace the old one when editing. */
    fun isDifferentTo(other: NPCInteractConfiguration): Boolean
}