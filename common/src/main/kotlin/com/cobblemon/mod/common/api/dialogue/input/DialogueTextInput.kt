/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue.input

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.dialogue.DialogueAction
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction

/**
 * A text input for dialogues where the player can enter free text.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class DialogueTextInput : DialogueInput {
    override var timeout: DialogueTimeout? = null
    var action: DialogueAction = FunctionDialogueAction { dialogue, _ -> dialogue.setPage(dialogue.currentPageIndex + 1) }
    override fun toMoLangStruct(activeInput: ActiveInput) = QueryStruct(hashMapOf())

    override fun handle(activeInput: ActiveInput, value: String) {
        action.invoke(activeInput.activeDialogue, value)
    }
}