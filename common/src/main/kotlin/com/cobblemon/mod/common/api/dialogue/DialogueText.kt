/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveString
import net.minecraft.network.chat.MutableComponent

/**
 * Some kind of text-resolving property. This is to hide where sometimes we want literals,
 * sometimes we want the text to come from a function, and sometimes we want it to be the
 * product of a MoLang script.
 *
 * @author Hiroku
 * @since December 29th, 2023
 */
interface DialogueText {
    companion object {
        val types = mutableMapOf<String, Class<out DialogueText>>(
            "expression" to ExpressionLikeDialogueText::class.java,
        )
    }

    operator fun invoke(activeDialogue: ActiveDialogue): MutableComponent
}

class FunctionDialogueText(val function: (ActiveDialogue) -> MutableComponent = { "".text() }) : DialogueText {
    override fun invoke(activeDialogue: ActiveDialogue) = function(activeDialogue)
}

class WrappedDialogueText(val text: MutableComponent = "".text()) : DialogueText {
    override fun invoke(activeDialogue: ActiveDialogue) = text.copy()
}

class ExpressionLikeDialogueText(val expression: ExpressionLike = "''".asExpressionLike()) : DialogueText {
    override fun invoke(activeDialogue: ActiveDialogue): MutableComponent {
        return activeDialogue.runtime.resolveString(expression).text()
    }
}