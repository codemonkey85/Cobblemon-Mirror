/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration.interaction

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.DialogueManager
import com.cobblemon.mod.common.api.dialogue.Dialogues
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * An [NPCInteractConfiguration] which starts a dialogue when the player interacts with the NPC.
 *
 * @author Hiroku
 * @since July 5th, 2024
 */
class DialogueNPCInteractionConfiguration : NPCInteractConfiguration {
    override val type: String = "dialogue"
    var dialogue = ResourceLocation.fromNamespaceAndPath("cobblemon", "dialogues/test.json")

    override fun interact(npc: NPCEntity, player: ServerPlayer): Boolean {
        val dialogue = Dialogues.dialogues[this.dialogue] ?: return false
        val currentDialogues = npc.brain.getMemory(CobblemonMemories.DIALOGUES).orElse(mutableListOf())
        val activeDialogue = DialogueManager.startDialogue(player, npc, dialogue)
        val newDialogues = currentDialogues + activeDialogue
        npc.brain.setMemory(CobblemonMemories.DIALOGUES, newDialogues)
        activeDialogue.completion.thenRun { onDialogueStopped(npc, activeDialogue) }
        return true
    }

    fun onDialogueStopped(npc: NPCEntity, activeDialogue: ActiveDialogue) {
        val currentDialogues = npc.brain.getMemory(CobblemonMemories.DIALOGUES).orElse(mutableListOf())
        val newDialogues = currentDialogues - activeDialogue
        if (newDialogues.isEmpty()) {
            npc.brain.eraseMemory(CobblemonMemories.DIALOGUES)
        } else {
            npc.brain.setMemory(CobblemonMemories.DIALOGUES, newDialogues)
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceLocation(dialogue)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        dialogue = buffer.readResourceLocation()
    }

    override fun writeToNBT(compoundTag: CompoundTag) {
        compoundTag.putString(DataKeys.NPC_INTERACT_DIALOGUE, dialogue.toString())
    }

    override fun readFromNBT(compoundTag: CompoundTag) {
        dialogue = ResourceLocation.parse(compoundTag.getString(DataKeys.NPC_INTERACT_DIALOGUE))
    }

    override fun isDifferentTo(other: NPCInteractConfiguration): Boolean {
        return other !is DialogueNPCInteractionConfiguration || other.dialogue != dialogue
    }
}