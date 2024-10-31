/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.dialogue.dto

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.ArtificialDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.DialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.ExpressionLikeDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.PlayerDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.ReferenceDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.input.DialogueAutoContinueInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueTextInput
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID
import net.minecraft.resources.ResourceLocation

class DialogueDTO : Encodable, Decodable {
    lateinit var dialogueId: UUID
    lateinit var background: ResourceLocation
    var speakers: Map<String, DialogueSpeakerDTO>? = null
    lateinit var currentPageDTO: DialoguePageDTO
    lateinit var dialogueInput: DialogueInputDTO

    constructor()
    constructor(activeDialogue: ActiveDialogue, includeFaces: Boolean) {
        this.dialogueId = activeDialogue.dialogueId
        this.speakers = if (includeFaces) activeDialogue.dialogueReference.speakers.mapNotNull { (key, value) ->
            if (value.face is ExpressionLikeDialogueFaceProvider) {
                val resolved = activeDialogue.runtime.resolve(value.face.providerExpression)
                if (resolved is ObjectValue<*> && resolved.obj is DialogueFaceProvider) {
                    key to DialogueSpeakerDTO(value.name?.invoke(activeDialogue), resolved.obj as DialogueFaceProvider)
                } else {
                    null
                }
            } else {
                key to DialogueSpeakerDTO(value.name?.invoke(activeDialogue), value.face)
            }
        }.toMap() else null
        this.currentPageDTO = DialoguePageDTO(activeDialogue.currentPage, activeDialogue)
        val input = activeDialogue.activeInput.dialogueInput
        this.dialogueInput = when (input) {
            is DialogueOptionSetInput -> DialogueInputDTO(input, activeDialogue)
            is DialogueAutoContinueInput -> DialogueInputDTO(input, activeDialogue)
            is DialogueTextInput -> DialogueInputDTO(input, activeDialogue)
            else -> DialogueInputDTO(activeDialogue) // Uses InputType.NONE
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(dialogueId)
        currentPageDTO.encode(buffer)
        dialogueInput.encode(buffer)
        buffer.writeNullable(speakers) { _, speakers ->
            buffer.writeInt(speakers.size)
            speakers.forEach { (key, value) ->
                buffer.writeString(key)
                buffer.writeNullable(value.name) { _, v -> buffer.writeText(v) }
                buffer.writeNullable(value.face) { _, v ->
                    buffer.writeString(
                        when (v) {
                            is ReferenceDialogueFaceProvider -> "reference"
                            is ArtificialDialogueFaceProvider -> "artificial"
                            else -> "player"
                        }
                    )
                }

                if (value.face is ArtificialDialogueFaceProvider) {
                    buffer.writeString(value.face.modelType)
                    buffer.writeIdentifier(value.face.identifier)
                    buffer.writeCollection(value.face.aspects) { _, aspect -> buffer.writeString(aspect) }
                    buffer.writeBoolean(value.face.isLeftSide)
                } else if (value.face is ReferenceDialogueFaceProvider) {
                    buffer.writeInt(value.face.entityId)
                    buffer.writeBoolean(value.face.isLeftSide)
                } else if (value.face is PlayerDialogueFaceProvider) {
                    buffer.writeUUID(value.face.playerId)
                    buffer.writeBoolean(value.face.isLeftSide)
                }
            }
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        dialogueId = buffer.readUUID()
        currentPageDTO = DialoguePageDTO()
        currentPageDTO.decode(buffer)
        dialogueInput = DialogueInputDTO()
        dialogueInput.decode(buffer)
        speakers = buffer.readNullable { _ ->
            val size = buffer.readInt()
            (0 until size).associate {
                val key = buffer.readString()
                val name = buffer.readNullable { buffer.readText().copy() }
                val faceType = buffer.readNullable { buffer.readString() }
                when (faceType) {
                    "reference" -> key to DialogueSpeakerDTO(name, ReferenceDialogueFaceProvider(buffer.readInt(), buffer.readBoolean()))
                    "artificial" -> {
                        val modelType = buffer.readString()
                        val identifier = buffer.readIdentifier()
                        val aspects = buffer.readList { buffer.readString() }.toSet()
                        val isLeftSide = buffer.readBoolean()
                        key to DialogueSpeakerDTO(name, ArtificialDialogueFaceProvider(modelType, identifier, aspects, isLeftSide))
                    }
                    "player" -> key to DialogueSpeakerDTO(name, PlayerDialogueFaceProvider(buffer.readUUID(), buffer.readBoolean()))
                    else -> key to DialogueSpeakerDTO(name, null)
                }
            }
        }
    }
}