/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.dialogue.dto

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.input.DialogueAutoContinueInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueTextInput
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Combined DTO file for all dialogue inputs. This is used to send the input data to the client.
 *
 * @author Hiroku
 * @since December 29th, 2023
 */
class DialogueInputDTO() : Encodable, Decodable {
    enum class InputType {
        OPTION,
        TEXT,
        AUTO_CONTINUE,
        NONE
    }

    var inputId: UUID = UUID.randomUUID()
    var inputType = InputType.NONE
    var deadline = -1F
    var showTimer = true

    var options = mutableListOf<DialogueOptionDTO>()
    var vertical = false

    var allowSkip = true

    constructor(optionSet: DialogueOptionSetInput, activeDialogue: ActiveDialogue) : this() {
        this.inputId = activeDialogue.activeInput.inputId
        this.options = optionSet.getVisibleOptions(activeDialogue).map {
            DialogueOptionDTO(
                text = it.text(activeDialogue),
                value = it.value,
                selectable = it.isSelectable(activeDialogue)
            )
        }.toMutableList()
        this.deadline = optionSet.timeout?.duration ?: -1F
        this.showTimer = optionSet.timeout?.showTimer ?: true
        this.inputType = InputType.OPTION
        this.vertical = optionSet.vertical
    }

    constructor(autoContinue: DialogueAutoContinueInput, activeDialogue: ActiveDialogue): this() {
        this.inputId = activeDialogue.activeInput.inputId
        this.deadline = autoContinue.timeout?.duration ?: -1F
        this.inputType = InputType.AUTO_CONTINUE
        this.allowSkip = autoContinue.allowSkip
        this.showTimer = autoContinue.showTimer
    }

    constructor(text: DialogueTextInput, activeDialogue: ActiveDialogue): this() {
        this.inputId = activeDialogue.activeInput.inputId
        this.deadline = text.timeout?.duration ?: -1F
        this.inputType = InputType.TEXT
        this.showTimer = text.timeout?.showTimer ?: true
    }

    constructor(activeDialogue: ActiveDialogue): this() {
        this.inputId = activeDialogue.activeInput.inputId
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(inputId)
        buffer.writeEnumConstant(inputType)
        buffer.writeFloat(deadline)
        buffer.writeBoolean(showTimer)
        when (inputType) {
            InputType.OPTION -> {
                buffer.writeBoolean(vertical)
                buffer.writeInt(options.size)
                options.forEach { it.encode(buffer) }
            }
            InputType.AUTO_CONTINUE -> {
                buffer.writeBoolean(allowSkip)
            }
            else -> // No extra data
                Unit
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        inputId = buffer.readUUID()
        inputType = buffer.readEnumConstant(InputType::class.java)
        deadline = buffer.readFloat()
        showTimer = buffer.readBoolean()
        when (inputType) {
            InputType.OPTION -> {
                vertical = buffer.readBoolean()
                val size = buffer.readInt()
                for (i in 0 until size) {
                    val option = DialogueOptionDTO()
                    option.decode(buffer)
                    options.add(option)
                }
            }
            InputType.AUTO_CONTINUE -> {
                allowSkip = buffer.readBoolean()
            }
            else -> // No extra data
                Unit
        }
    }
}