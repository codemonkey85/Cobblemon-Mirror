/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

/**
 * A dialogue that could be sent to players.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class Dialogue(
    val pages: List<DialoguePage> = mutableListOf(),
    val background: ResourceLocation = DEFAULT_BACKGROUND,
    val escapeAction: DialogueAction = FunctionDialogueAction { dialogue, _ -> dialogue.close() },
    val speakers: Map<String, DialogueSpeaker> = emptyMap(),
    val initializationAction: DialogueAction = FunctionDialogueAction { _, _ -> }
) {
    companion object {
        val DEFAULT_BACKGROUND = cobblemonResource("textures/gui/dialogue/dialogue_box.png")

        @JvmOverloads
        fun of(
            pages: Iterable<DialoguePage>,
            background: ResourceLocation = DEFAULT_BACKGROUND,
            escapeAction: ExpressionLike,
            speakers: Map<String, DialogueSpeaker>
        ): Dialogue {
            return Dialogue(
                pages = pages.toList(),
                escapeAction = ExpressionLikeDialogueAction(escapeAction),
                background = background,
                speakers = speakers
            )
        }

        @JvmOverloads
        fun of(
            pages: Iterable<DialoguePage>,
            background: ResourceLocation = DEFAULT_BACKGROUND,
            escapeAction: (ActiveDialogue) -> Unit,
            speakers: Map<String, DialogueSpeaker>
        ): Dialogue {
            val dialogue = Dialogue(
                pages = pages.toList(),
                background = background,
                escapeAction = FunctionDialogueAction { activeDialogue, _ -> escapeAction(activeDialogue) },
                speakers = speakers
            )
            return dialogue
        }
    }
}