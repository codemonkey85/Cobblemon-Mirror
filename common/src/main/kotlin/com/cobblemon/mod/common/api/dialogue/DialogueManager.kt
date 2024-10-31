/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueClosedPacket
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueOpenedPacket
import com.cobblemon.mod.common.util.activeDialogue
import com.cobblemon.mod.common.util.withNPCValue
import java.util.UUID
import net.minecraft.server.level.ServerPlayer

/**
 * Manages the active dialogues for players. Map is indexed by player UUID.
 * You really need to make sure any dialogues you start with a player go through this otherwise
 * the player won't be able to close the dialogue.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
object DialogueManager {
    val activeDialogues = mutableMapOf<UUID, ActiveDialogue>()

    fun startDialogue(playerEntity: ServerPlayer, dialogue: Dialogue): ActiveDialogue {
        val activeDialogue = ActiveDialogue(playerEntity, dialogue)
        startDialogue(activeDialogue)
        return activeDialogue
    }

    fun startDialogue(playerEntity: ServerPlayer, npcEntity: NPCEntity, dialogue: Dialogue): ActiveDialogue {
        val activeDialogue = ActiveDialogue(playerEntity, dialogue)
        activeDialogue.runtime.withNPCValue("npc", npcEntity)
        activeDialogue.npc = npcEntity
        startDialogue(activeDialogue)
        return activeDialogue
    }

    fun startDialogue(activeDialogue: ActiveDialogue) {
        activeDialogue.initialize()
        if (!activeDialogue.completion.isDone) {
            activeDialogues[activeDialogue.playerEntity.uuid] = activeDialogue
        }
    }

    fun stopDialogue(playerEntity: ServerPlayer) {
        val activeDialogue = playerEntity.activeDialogue ?: return
        DialogueClosedPacket(activeDialogue.dialogueId).sendToPlayer(playerEntity)
        activeDialogues.remove(activeDialogue.dialogueId)
    }
}