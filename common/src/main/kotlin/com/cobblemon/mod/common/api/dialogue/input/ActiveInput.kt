/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue.input

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.util.server
import java.util.UUID

/**
 * An active input waiting for the player as part of an [ActiveDialogue]. The purpose of this is to
 * maintain some data integrity and track how long it's taken to choose an action.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class ActiveInput(
    val activeDialogue: ActiveDialogue,
    val dialogueInput: DialogueInput
) {
    val inputId = UUID.randomUUID()
    val startTime = server()!!.overworld().gameTime

    val struct = toMoLangStruct()

    fun handle(input: String) {
        val secondsToChoose = (server()!!.overworld().gameTime - startTime) / 20F
        activeDialogue.runtime.environment.setSimpleVariable("seconds_taken_to_input", DoubleValue(secondsToChoose))
        dialogueInput.handle(this, input)
    }

    fun toMoLangStruct(): MoValue {
        return dialogueInput.toMoLangStruct(this)
    }
}