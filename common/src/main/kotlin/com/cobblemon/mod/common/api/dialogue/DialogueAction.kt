/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.util.resolve

/**
 * Something that can happen in the context of an [ActiveDialogue]. The input field is nullable because I'm reusing
 * this for dialogue input timeouts which don't have an input. It'll be fine.
 *
 * @author Hiroku
 * @since December 29th, 2023
 */
interface DialogueAction {
    companion object {
        @JvmStatic
        val types = mutableMapOf<String, Class<out DialogueAction>>()
    }

    operator fun invoke(dialogue: ActiveDialogue, input: String? = null)
}

class FunctionDialogueAction(val consumer: (ActiveDialogue, String?) -> Unit) : DialogueAction {
    override fun invoke(dialogue: ActiveDialogue, input: String?) {
        consumer(dialogue, input)
    }
}

class ExpressionLikeDialogueAction(val expression: ExpressionLike) : DialogueAction {
    override fun invoke(dialogue: ActiveDialogue, input: String?) {
        if (input != null) {
            dialogue.runtime.environment.setSimpleVariable("selected_option", StringValue(input))
        }
        dialogue.runtime.resolve(
            expression,
            mapOf("player" to dialogue.playerStruct, "npc" to (dialogue.npc?.struct ?: QueryStruct(hashMapOf())))
        )
    }
}