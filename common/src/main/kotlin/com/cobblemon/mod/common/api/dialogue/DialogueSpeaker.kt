/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.cobblemon.mod.common.api.text.text
import net.minecraft.network.chat.MutableComponent

/**
 * A kind of speaker in a dialogue. They include a name and a face, each optional.
 *
 * @author Hiroku
 * @since January 1st, 2024
 */
class DialogueSpeaker(
    val name: DialogueText? = null,
    val face: DialogueFaceProvider? = null
) {
    fun of(name: MutableComponent = "".text(), face: DialogueFaceProvider? = null) = DialogueSpeaker(WrappedDialogueText(name), face)
}