/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.struct.MoStruct
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.dialogue.input.DialogueInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueNoInput
import com.google.gson.JsonArray
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

/**
 * A page of a dialogue. This has lines of text and also some kind of input expected from the player.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class DialoguePage(
    var id: String = "",
    var speaker: String? = null,
    var lines: MutableList<DialogueText> = mutableListOf(),
    var input: DialogueInput = DialogueNoInput(),
    var background: ResourceLocation? = null,
    var clientActions: MutableList<Expression> = mutableListOf(),
    var escapeAction: DialogueAction? = null,
) {
    companion object {
        @JvmOverloads
        fun of(
            /** The ID is optional, but if you want to be able to jump to this page from other pages then you probably want to set this. */
            id: String = "",
            speaker: String? = null,
            lines: Iterable<MutableComponent>,
            input: DialogueInput = DialogueNoInput(),
            background: ResourceLocation? = null,
            clientActions: Iterable<Expression> = emptyList(),
            /** The thing to do when the player presses ESC while on this page. If null, falls back to the same property on [Dialogue]. */
            escapeAction: ((ActiveDialogue) -> Unit)? = null,
        ): DialoguePage {
            return DialoguePage(
                id = id,
                speaker = speaker,
                lines = lines.map { WrappedDialogueText(it) }.toMutableList(),
                input = input,
                background = background,
                clientActions = clientActions.toMutableList(),
                escapeAction = escapeAction?.let { func -> FunctionDialogueAction { activeDialogue, _ -> func(activeDialogue) } }
            )
        }
    }

    fun toMoLangStruct(activeDialogue: ActiveDialogue): MoStruct {
        return QueryStruct(
            hashMapOf(
                "id" to java.util.function.Function { _ -> StringValue(id) },
                "input" to java.util.function.Function { _ -> activeDialogue.activeInput.struct },
                "lines" to java.util.function.Function { _ ->
                    val array = JsonArray()
                    lines.forEach { array.add(it(activeDialogue).string) }
                    MoValue.of(array)
                }
            )
        )
    }
}